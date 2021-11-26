package com.github.jorgebsa.spring.demo.ws;

import com.github.jorgebsa.spring.demo.ApplicationTests;
import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.base.SaveNoteRequest;
import com.github.jorgebsa.spring.demo.base.SaveNoteResponse;
import com.github.jorgebsa.spring.demo.base.UpdateNoteRequest;
import com.github.jorgebsa.spring.demo.util.NoteFactory;
import com.github.jorgebsa.spring.demo.util.UserData;
import com.github.jorgebsa.spring.demo.validation.ErrorMessage;
import com.github.jorgebsa.spring.demo.validation.Violation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class NoteControllerPersistenceTest extends ApplicationTests {

    @Autowired
    private NoteFactory factory;

    @AfterEach
    void tearDown() {
        factory.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void saveNote(UserData userData) {
        var content = "some content";
        var request = new SaveNoteRequest(content);
        var authorizationHeader = getAuthorizationHeader(userData);
        var httpEntity = new HttpEntity<>(request, authorizationHeader);

        var responseEntity = template.exchange("/notes", POST, httpEntity, SaveNoteResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(CREATED);

        var body = responseEntity.getBody();
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.username())
                        .as("Response's username should match expected value")
                        .isEqualTo(userData.username()),
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
                arguments(UserData.ADMIN, "", violation),
                arguments(UserData.ADMIN, " ", violation),
                arguments(UserData.ADMIN, "       ", violation),
                arguments(UserData.SOME_USER, "", violation),
                arguments(UserData.SOME_USER, " ", violation),
                arguments(UserData.SOME_USER, "       ", violation)
        );
    }

    @MethodSource
    @ParameterizedTest
    void saveNoteWithInvalidPayload(UserData userData, String content, Violation violation) {
        var headers = getAuthorizationAndContentTypeHeaders(userData);
        var httpEntity = new HttpEntity<>("{\"content\":\"" + content + "\"}", headers);

        var responseEntity = template.exchange("/notes", POST, httpEntity, ErrorMessage.class);
        assertErrorMessage(BAD_REQUEST, responseEntity, Violation.asMaps(violation));
    }

    @Test
    void saveNoteWithoutToken() {
        var content = "some content";
        var request = new SaveNoteRequest(content);
        var httpEntity = new HttpEntity<>(request);

        var responseEntity = template.exchange("/notes", POST, httpEntity, SaveNoteResponse.class);

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
    void deleteNote(UserData userData) {
        var headers = getAuthorizationHeader(userData);
        var entity = new HttpEntity<Void>(headers);

        var content = "test content";
        var saved = factory.insert(userData.username(), content);

        var id = saved.getId();

        var responseEntity = template.exchange("/notes/{id}", DELETE, entity, NoteDTO.class, id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        var body = responseEntity.getBody();
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.username())
                        .as("Response's username should match expected value")
                        .isEqualTo(userData.username()),
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

        assertThat(factory.findById(id)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void deleteByIdWhenNotFound(UserData userData) {
        var headers = getAuthorizationHeader(userData);
        var entity = new HttpEntity<Void>(headers);

        var responseEntity = template.exchange("/notes/this-wont-be-found", DELETE, entity, NoteDTO.class);
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
    void deleteByIdWithoutToken() {
        var responseEntity = template.exchange("/notes/{id}", DELETE, null, NoteDTO.class, "this-will-redirect-to-login");
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
    void updateNote(UserData userData) {
        var saved = factory.insert(userData.username(), "this is such an interesting content");
        var id = saved.getId();

        var content = "new content";
        var request = new UpdateNoteRequest(content, saved.getVersion());
        var headers = getAuthorizationHeader(userData);
        var httpEntity = new HttpEntity<>(request, headers);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, NoteDTO.class, id);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        var body = responseEntity.getBody();
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.username())
                        .as("Response's username should match expected value")
                        .isEqualTo(userData.username()),
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
                arguments(UserData.ADMIN, "", 1, List.of(contentViolation)),
                arguments(UserData.ADMIN, " ", 1, List.of(contentViolation)),
                arguments(UserData.ADMIN, "       ", 1, List.of(contentViolation)),
                arguments(UserData.ADMIN, "fine", 0, List.of(versionViolation)),
                arguments(UserData.ADMIN, "fine", -1, List.of(versionViolation)),
                arguments(UserData.ADMIN, " ", -1, List.of(contentViolation, versionViolation)),
                arguments(UserData.SOME_USER, "", 1, List.of(contentViolation)),
                arguments(UserData.SOME_USER, " ", 1, List.of(contentViolation)),
                arguments(UserData.SOME_USER, "       ", 1, List.of(contentViolation)),
                arguments(UserData.SOME_USER, "fine", 0, List.of(versionViolation)),
                arguments(UserData.SOME_USER, "fine", -1, List.of(versionViolation)),
                arguments(UserData.SOME_USER, " ", -1, List.of(contentViolation, versionViolation))
        );
    }

    @MethodSource
    @ParameterizedTest
    void updateNoteWithInvalidPayload(UserData userData, String content, long version, List<Violation> violations) {
        var saved = factory.insert(userData.username(), "yay!");
        var headers = getAuthorizationAndContentTypeHeaders(userData);
        var httpEntity = new HttpEntity<>("{\"content\":\"" + content + "\",\"version\":" + version + "}", headers);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, ErrorMessage.class, saved.getId());
        assertErrorMessage(BAD_REQUEST, responseEntity, Violation.asMaps(violations));
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void updateNoteWithIncorrectVersion(UserData userData) {
        var saved = factory.insert(userData.username(), "this is such an interesting content");
        saved.setContent("updated already");
        saved = factory.save(saved);
        var id = saved.getId();

        var incorrectVersion = saved.getVersion() - 1L;
        var request = new UpdateNoteRequest("new content", incorrectVersion);
        var headers = getAuthorizationHeader(userData);
        var httpEntity = new HttpEntity<>(request, headers);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, ErrorMessage.class, id);
        assertErrorMessage(CONFLICT, responseEntity, Violation.asMaps(new Violation("version", "incorrect value: " + incorrectVersion)));
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void updateNoteWhenDifferentUser(UserData userData) {
        var saved = factory.insert("different-user", "very interesting content indeed");
        var id = saved.getId();

        var request = new UpdateNoteRequest("new content", saved.getVersion());
        var headers = getAuthorizationHeader(userData);
        var httpEntity = new HttpEntity<>(request, headers);

        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, ErrorMessage.class, id);
        assertErrorMessage(FORBIDDEN, responseEntity, Violation.asMaps(new Violation("id", "note [" + id + "] does not belong to you")));
    }

    @ParameterizedTest
    @MethodSource("userDataSource")
    void updateNoteWhenNotFound(UserData userData) {
        var headers = getAuthorizationHeader(userData);
        var request = new UpdateNoteRequest("new content", 2L);
        var httpEntity = new HttpEntity<>(request, headers);

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

    @Test
    void updateNoteWithoutToken() {
        var request = new UpdateNoteRequest("new content", 2L);
        var httpEntity = new HttpEntity<>(request);
        var responseEntity = template.exchange("/notes/{id}", PUT, httpEntity, NoteDTO.class, "this-will-redirect-to-login");
        assertAll(
                () -> assertThat(responseEntity.getStatusCode())
                        .as("status code should be FOUND")
                        .isEqualTo(FOUND),
                () -> assertThat(responseEntity.getHeaders().get(HttpHeaders.LOCATION))
                        .as("location header should point to the login url")
                        .isEqualTo(List.of(getLoginURL()))
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

}
