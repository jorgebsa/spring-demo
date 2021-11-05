package com.example.kore.spring.ws;

import com.example.kore.spring.ApplicationTests;
import com.example.kore.spring.base.NoteDTO;
import com.example.kore.spring.base.SaveNoteRequest;
import com.example.kore.spring.base.SaveNoteResponse;
import com.example.kore.spring.dao.Note;
import com.example.kore.spring.dao.NoteRepository;
import com.example.kore.spring.service.NoteMapper;
import com.example.kore.spring.util.ReferenceUtil;
import com.example.kore.spring.validation.ErrorMessage;
import com.example.kore.spring.validation.Violation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public class NoteControllerTest extends ApplicationTests {

    @Autowired
    protected TestRestTemplate template;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private NoteRepository repository;

    @Autowired
    private NoteMapper mapper;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void saveNote() {
        var content = "some content";
        var request = new SaveNoteRequest(content);
        var httpEntity = new HttpEntity<>(request);

        var millis = System.currentTimeMillis();
        var responseEntity = template.exchange("/notes", POST, httpEntity, SaveNoteResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(CREATED);

        var body = responseEntity.getBody();
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.username())
                        .as("Response's username should match expected value")
                        .isEqualTo("some-user"),
                () -> assertThat(body.content())
                        .as("Response's content should match expected value")
                        .isEqualTo(content),
                () -> assertThat(body.id())
                        .as("Response's id should not be null")
                        .isNotNull(),
                () -> assertThat(body.createdAt())
                        .as("Response's createdAt should be a value in expected range (millis before request to +3s)")
                        .isBetween(millis, millis + 3000L),
                () -> assertThat(body.version())
                        .as("Response's version should be 1 as this is a new Note")
                        .isEqualTo(1L)
        );
    }

    static Stream<Arguments> saveNoteWithInvalidPayload() {
        var violations = List.of(new Violation("content", "must not be blank"));
        return Stream.of(
                arguments("", violations),
                arguments(" ", violations),
                arguments("       ", violations)
        );
    }

    @MethodSource
    @ParameterizedTest
    void saveNoteWithInvalidPayload(String content, List<Violation> violations) {
        var headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/json");
        var httpEntity = new HttpEntity<>("{\"content\":\"" + content + "\"}", headers);

        var responseEntity = template.exchange("/notes", POST, httpEntity, ErrorMessage.class);
        assertErrorMessage(UNPROCESSABLE_ENTITY, responseEntity, Violation.asMaps(violations));
    }

    @Test
    void findById() {
        var username = "random-user";
        var content = "test content";

        var saved = repository.save(new Note(username, content));

        var responseEntity = template.exchange("/notes/{id}", GET, null, NoteDTO.class, saved.getId());
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        var body = responseEntity.getBody();
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.username())
                        .as("Response's username should match expected value")
                        .isEqualTo(username),
                () -> assertThat(body.content())
                        .as("Response's content should match expected value")
                        .isEqualTo(content),
                () -> assertThat(body.id())
                        .as("Response's id should match expected value")
                        .isEqualTo(saved.getId()),
                () -> assertThat(body.createdAt())
                        .as("Response's createdAt should match expected value")
                        .isEqualTo(saved.getCreatedAt().toEpochMilli()),
                () -> assertThat(body.lastModifiedAt())
                        .as("Response's lastModifiedAt should match expected value")
                        .isEqualTo(saved.getLastModifiedAt().toEpochMilli()),
                () -> assertThat(body.version())
                        .as("Response's version should match expected value")
                        .isEqualTo(saved.getVersion())
        );

    }

    @Test
    void findByIdWhenNotFound() {
        var responseEntity = template.exchange("/notes/this-wont-be-found", GET, null, NoteDTO.class);
        assertAll(
                () -> assertThat(responseEntity.getStatusCode())
                        .as("status code should be NOT FOUND")
                        .isEqualTo(NOT_FOUND),
                () -> assertThat(responseEntity.getBody())
                        .as("body should be null")
                        .isNull()
        );
    }

    @Test
    void findDefaultPage() throws JsonProcessingException {
        var count = 30;
        var savedNotes = saveNotes(count);
        var pageSize = 20;
        var expectedContent = savedNotes.subList(0, pageSize).stream().map(mapper::toDTO).toList();

        var responseEntity = template.exchange("/notes", GET, null, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        var resultPage = objectMapper.readValue(responseEntity.getBody(), ReferenceUtil.NOTE_RESULT_PAGE_TYPE_REFERENCE);
        assertAll(
                () -> assertThat(resultPage.getNumber())
                        .as("Page number should match expected value")
                        .isEqualTo(0),
                () -> assertThat(resultPage.getSize())
                        .as("Page size should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.getTotalPages())
                        .as("Total pages should match expected value")
                        .isEqualTo(2),
                () -> assertThat(resultPage.getTotalElements())
                        .as("Total elements should match expected value")
                        .isEqualTo(count),
                () -> assertThat(resultPage.getNumberOfElements())
                        .as("Number of elements should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.getContent())
                        .as("Content should match expected value")
                        .containsExactlyElementsOf(expectedContent)
        );
    }

    @Test
    void findCustomPage() throws JsonProcessingException {
        var count = 30;
        var savedNotes = saveNotes(count);
        var pageSize = 5;
        var pageIdx = 3;
        var startIdx = pageSize * pageIdx;
        var expectedContent = savedNotes.subList(startIdx, startIdx + pageSize).stream().map(mapper::toDTO).toList();

        var responseEntity = template.exchange("/notes?page={p}&size={s}", GET, null, String.class, pageIdx, pageSize);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        var resultPage = objectMapper.readValue(responseEntity.getBody(), ReferenceUtil.NOTE_RESULT_PAGE_TYPE_REFERENCE);
        assertAll(
                () -> assertThat(resultPage.getNumber())
                        .as("Page number should match expected value")
                        .isEqualTo(pageIdx),
                () -> assertThat(resultPage.getSize())
                        .as("Page size should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.getTotalPages())
                        .as("Total pages should match expected value")
                        .isEqualTo(count / pageSize),
                () -> assertThat(resultPage.getTotalElements())
                        .as("Total elements should match expected value")
                        .isEqualTo(count),
                () -> assertThat(resultPage.getNumberOfElements())
                        .as("Number of elements should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.getContent())
                        .as("Content should match expected value")
                        .containsExactlyElementsOf(expectedContent)
        );
    }

    @Test
    void deleteNote() {
        var username = "random-user";
        var content = "test content";
        var saved = repository.save(new Note(username, content));

        var id = saved.getId();

        var responseEntity = template.exchange("/notes/{id}", DELETE, null, NoteDTO.class, id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        var body = responseEntity.getBody();
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.username())
                        .as("Response's username should match expected value")
                        .isEqualTo(username),
                () -> assertThat(body.content())
                        .as("Response's content should match expected value")
                        .isEqualTo(content),
                () -> assertThat(body.id())
                        .as("Response's id should match expected value")
                        .isEqualTo(id),
                () -> assertThat(body.createdAt())
                        .as("Response's createdAt should match expected value")
                        .isEqualTo(saved.getCreatedAt().toEpochMilli()),
                () -> assertThat(body.lastModifiedAt())
                        .as("Response's lastModifiedAt should match expected value")
                        .isEqualTo(saved.getLastModifiedAt().toEpochMilli()),
                () -> assertThat(body.version())
                        .as("Response's version should match expected value")
                        .isEqualTo(saved.getVersion())
        );

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void deleteByIdWhenNotFound() {
        var responseEntity = template.exchange("/notes/this-wont-be-found", DELETE, null, NoteDTO.class);
        assertAll(
                () -> assertThat(responseEntity.getStatusCode())
                        .as("status code should be NOT FOUND")
                        .isEqualTo(NOT_FOUND),
                () -> assertThat(responseEntity.getBody())
                        .as("body should be null")
                        .isNull()
        );
    }

    protected void assertErrorMessage(HttpStatus status, ResponseEntity<ErrorMessage> responseEntity, Object errors) {
        var body = responseEntity.getBody();
        assertAll(
                () -> {
                    assertThat(body)
                            .as("Response body shouldn't be null")
                            .isNotNull();
                    assertAll(
                            () -> assertThat(body.status())
                                    .as("Body: 'status' should match expected value")
                                    .isEqualTo(status.value()),
                            () -> assertThat(body.errors())
                                    .as("Body: 'errors' should match expected values")
                                    .isEqualTo(errors),
                            () -> assertTimestampIsRecent(body.timestamp())
                    );
                },
                () -> assertThat(responseEntity.getStatusCode())
                        .as("ResponseEntity: status should match expected value")
                        .isEqualTo(status)
        );
    }

    private void assertTimestampIsRecent(String timestamp) {
        var currentTimeMillis = System.currentTimeMillis();
        var zonedDateTime = ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        var timestampAsMillis = zonedDateTime.toInstant().toEpochMilli();

        assertThat(timestampAsMillis).isBetween(
                currentTimeMillis - TimeUnit.SECONDS.toMillis(4),
                currentTimeMillis
        );
    }

    private ArrayList<Note> saveNotes(int count) {
        return saveNotes(count, 3);
    }

    private ArrayList<Note> saveNotes(int count, int diffUsers) {
        var savedNotes = new ArrayList<Note>(count);
        for (int i = 0 ; i < count ; i++) {
            var username = "user-" + (i % diffUsers);
            var content = "content of note #" + (i + 1);
            savedNotes.add(new Note(username, content));
        }
        repository.saveAll(savedNotes);
        return savedNotes;
    }
}
