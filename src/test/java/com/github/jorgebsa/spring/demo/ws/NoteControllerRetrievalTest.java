package com.github.jorgebsa.spring.demo.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.jorgebsa.spring.demo.ApplicationTests;
import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.service.NoteMapper;
import com.github.jorgebsa.spring.demo.util.NoteFactory;
import com.github.jorgebsa.spring.demo.util.UserData;
import com.github.jorgebsa.spring.demo.validation.ErrorMessage;
import com.github.jorgebsa.spring.demo.validation.Violation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static com.github.jorgebsa.spring.demo.util.UserData.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class NoteControllerRetrievalTest extends ApplicationTests {

    @Autowired
    private NoteFactory factory;

    @Autowired
    private NoteMapper mapper;

    @AfterEach
    void tearDown() {
        factory.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void findById(UserData userData) {
        var username = "random-user";
        var content = "test content";

        var saved = factory.insert(username, content);

        var headers = getAuthorizationHeader(userData);
        var entity = new HttpEntity<Void>(headers);

        var responseEntity = template.exchange("/notes/{id}", GET, entity, NoteDTO.class, saved.getId());
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

    @ParameterizedTest
    @MethodSource("userDataSource")
    void findByIdWhenNotFound(UserData userData) {
        var headers = getAuthorizationHeader(userData);
        var entity = new HttpEntity<Void>(headers);

        var responseEntity = template.exchange("/notes/this-wont-be-found", GET, entity, NoteDTO.class);
        assertAll(
                () -> assertThat(responseEntity.getStatusCode())
                        .as("status code should be NOT FOUND")
                        .isEqualTo(NOT_FOUND),
                () -> assertThat(responseEntity.getBody())
                        .as("body should be null")
                        .isNull()
        );
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void findByIdWhenInvalid(UserData userData) {
        var headers = getAuthorizationHeader(userData);
        var entity = new HttpEntity<Void>(headers);

        var expectedViolation = Violation.asMaps(new Violation("id", "must not be blank"));
        var responseEntity = template.exchange("/notes/{id}", GET, entity, ErrorMessage.class, "    ");
        assertErrorMessage(BAD_REQUEST, responseEntity, expectedViolation);
    }

    @Test
    void findByIdWithoutToken() {
        var saved = factory.insert(ADMIN.username(), "content");
        var responseEntity = template.exchange("/notes/{id}", GET, null, ErrorMessage.class, saved.getId());

        assertAll(
                () -> assertThat(responseEntity.getStatusCode())
                        .as("status code should be FOUND")
                        .isEqualTo(FOUND),
                () -> assertThat(responseEntity.getHeaders().get(HttpHeaders.LOCATION))
                        .as("location header should point to the login url")
                        .isEqualTo(List.of(getLoginURL()))
        );
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void findDefaultPage(UserData userData) throws JsonProcessingException {
        var headers = getAuthorizationHeader(userData);
        var entity = new HttpEntity<Void>(headers);

        var count = 30;
        var savedNotes = factory.insertNotes(count);
        var pageSize = 20;
        var expectedContent = savedNotes.subList(0, pageSize).stream().map(mapper::toDTO).toList();

        var responseEntity = template.exchange("/notes", GET, entity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        var resultPage = objectMapper.readValue(responseEntity.getBody(), NOTE_RESULT_PAGE_TYPE_REFERENCE);
        assertAll(
                () -> assertThat(resultPage.number())
                        .as("Page number should match expected value")
                        .isEqualTo(0),
                () -> assertThat(resultPage.size())
                        .as("Page size should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.totalPages())
                        .as("Total pages should match expected value")
                        .isEqualTo(2),
                () -> assertThat(resultPage.totalElements())
                        .as("Total elements should match expected value")
                        .isEqualTo(count),
                () -> assertThat(resultPage.numberOfElements())
                        .as("Number of elements should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.content())
                        .as("Content should match expected value")
                        .containsExactlyElementsOf(expectedContent)
        );
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void findCustomPage(UserData userData) throws JsonProcessingException {
        var headers = getAuthorizationHeader(userData);
        var entity = new HttpEntity<Void>(headers);

        var count = 30;
        var savedNotes = factory.insertNotes(count);
        var pageSize = 5;
        var pageIdx = 3;
        var startIdx = pageSize * pageIdx;
        var expectedContent = savedNotes.subList(startIdx, startIdx + pageSize).stream().map(mapper::toDTO).toList();

        var responseEntity = template.exchange("/notes?page={p}&size={s}", GET, entity, String.class, pageIdx, pageSize);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        var resultPage = objectMapper.readValue(responseEntity.getBody(), NOTE_RESULT_PAGE_TYPE_REFERENCE);
        assertAll(
                () -> assertThat(resultPage.number())
                        .as("Page number should match expected value")
                        .isEqualTo(pageIdx),
                () -> assertThat(resultPage.size())
                        .as("Page size should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.totalPages())
                        .as("Total pages should match expected value")
                        .isEqualTo(count / pageSize),
                () -> assertThat(resultPage.totalElements())
                        .as("Total elements should match expected value")
                        .isEqualTo(count),
                () -> assertThat(resultPage.numberOfElements())
                        .as("Number of elements should match expected value")
                        .isEqualTo(pageSize),
                () -> assertThat(resultPage.content())
                        .as("Content should match expected value")
                        .containsExactlyElementsOf(expectedContent)
        );
    }

    @Test
    void findPageWithoutToken() {
        var responseEntity = template.exchange("/notes", GET, null, String.class);

        assertAll(
                () -> assertThat(responseEntity.getStatusCode())
                        .as("status code should be FOUND")
                        .isEqualTo(FOUND),
                () -> assertThat(responseEntity.getHeaders().get(HttpHeaders.LOCATION))
                        .as("location header should point to the login url")
                        .isEqualTo(List.of(getLoginURL()))
        );
    }

}
