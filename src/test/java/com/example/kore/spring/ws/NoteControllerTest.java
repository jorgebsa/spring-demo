package com.example.kore.spring.ws;

import com.example.kore.spring.ApplicationTests;
import com.example.kore.spring.base.NoteDTO;
import com.example.kore.spring.base.SaveNoteRequest;
import com.example.kore.spring.base.SaveNoteResponse;
import com.example.kore.spring.base.UpdateNoteRequest;
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
import org.springframework.util.MultiValueMap;

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
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

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
                () -> assertMillisIsRecent(body.createdAt()),
                () -> assertThat(body.version())
                        .as("Response's version should be 1 as this is a new Note")
                        .isEqualTo(1L)
        );
    }

    static Stream<Arguments> saveNoteWithInvalidPayload() {
        var violation = new Violation("content", "must not be blank");
        return Stream.of(
                arguments("", violation),
                arguments(" ", violation),
                arguments("       ", violation)
        );
    }

    @MethodSource
    @ParameterizedTest
    void saveNoteWithInvalidPayload(String content, Violation violation) {
        var headers = getContentTypeHeader();
        var httpEntity = new HttpEntity<>("{\"content\":\"" + content + "\"}", headers);

        var responseEntity = template.exchange("/notes", POST, httpEntity, ErrorMessage.class);
        assertErrorMessage(BAD_REQUEST, responseEntity, Violation.asMaps(violation));
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
    void findByIdWhenInvalid() {
        var expectedViolation = Violation.asMaps(new Violation("id", "must not be blank"));
        var responseEntity = template.exchange("/notes/{id}", GET, null, ErrorMessage.class, "    ");
        assertErrorMessage(BAD_REQUEST, responseEntity, expectedViolation);
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

    @Test
    void updateNote() {
        var username = "some-user";
        var saved = saveNote(username, "this is such an interesting content");
        var id = saved.getId();

        var content = "new content";
        var request = new UpdateNoteRequest(content, saved.getVersion());
        var httpEntity = new HttpEntity<>(request);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, NoteDTO.class, id);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        var body = responseEntity.getBody();
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.username())
                        .as("Response's username should match expected value")
                        .isEqualTo(username),
                () -> assertThat(body.content())
                        .as("Response's content should match updated value")
                        .isEqualTo(content),
                () -> assertThat(body.id())
                        .as("Response's id should match expected value")
                        .isEqualTo(id),
                () -> assertThat(body.createdAt())
                        .as("Response's createdAt should match expected value")
                        .isEqualTo(saved.getCreatedAt().toEpochMilli()),
                () -> assertMillisIsRecent(body.lastModifiedAt()),
                () -> assertThat(body.version())
                        .as("Response's version should have been increased by 1")
                        .isEqualTo(saved.getVersion() + 1)
        );
    }

    static Stream<Arguments> updateNoteWithInvalidPayload() {
        var contentViolation = new Violation("content", "must not be blank");
        var versionViolation = new Violation("version", "must be greater than 0");
        return Stream.of(
                arguments("", 1, List.of(contentViolation)),
                arguments(" ", 1, List.of(contentViolation)),
                arguments("       ", 1, List.of(contentViolation)),
                arguments("fine", 0, List.of(versionViolation)),
                arguments("fine", -1, List.of(versionViolation)),
                arguments(" ", -1, List.of(contentViolation, versionViolation))
        );
    }

    @MethodSource
    @ParameterizedTest
    void updateNoteWithInvalidPayload(String content, long version, List<Violation> violations) {
        var saved = saveNote("some-user", "yay!");
        var headers = getContentTypeHeader();
        var httpEntity = new HttpEntity<>("{\"content\":\"" + content + "\",\"version\":" + version + "}", headers);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, ErrorMessage.class, saved.getId());
        assertErrorMessage(BAD_REQUEST, responseEntity, Violation.asMaps(violations));
    }

    @Test
    void updateNoteWithIncorrectVersion() {
        var username = "some-user";
        var saved = saveNote(username, "this is such an interesting content");
        saved.setContent("updated already");
        saved = repository.save(saved);
        var id = saved.getId();

        var incorrectVersion = saved.getVersion() - 1L;
        var request = new UpdateNoteRequest("new content", incorrectVersion);
        var httpEntity = new HttpEntity<>(request);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, ErrorMessage.class, id);
        assertErrorMessage(CONFLICT, responseEntity, Violation.asMaps(new Violation("version", "incorrect value: " + incorrectVersion)));
    }

    @Test
    void updateNoteWhenDifferentUser() {
        var saved = saveNote("different-user", "very interesting content indeed");
        var id = saved.getId();

        var request = new UpdateNoteRequest("new content", saved.getVersion());
        var httpEntity = new HttpEntity<>(request);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, ErrorMessage.class, id);
        assertErrorMessage(FORBIDDEN, responseEntity, Violation.asMaps(new Violation("id", "note [" + id + "] does not belong to you")));
    }

    @Test
    void updateNoteWhenNotFound() {
        var request = new UpdateNoteRequest("new content", 2L);
        var httpEntity = new HttpEntity<>(request);
        var responseEntity = template.exchange("/notes/this-wont-be-found", PUT, httpEntity, NoteDTO.class);
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

    private MultiValueMap<String, String> getContentTypeHeader() {
        var headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/json");
        return headers;
    }

    private void assertTimestampIsRecent(String timestamp) {
        var zonedDateTime = ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        var timestampAsMillis = zonedDateTime.toInstant().toEpochMilli();

        assertMillisIsRecent(timestampAsMillis);
    }

    private void assertMillisIsRecent(long millis) {
        var currentTimeMillis = System.currentTimeMillis();
        var start = currentTimeMillis - TimeUnit.SECONDS.toMillis(3);
        assertThat(millis).isBetween(start, currentTimeMillis);
    }

    private Note saveNote(String username, String content) {
        return repository.insert(new Note(username, content));
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
        repository.insert(savedNotes);
        return savedNotes;
    }
}
