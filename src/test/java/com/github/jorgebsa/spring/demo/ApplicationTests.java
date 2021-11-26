package com.github.jorgebsa.spring.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jorgebsa.spring.demo.base.NoteDTO;
import com.github.jorgebsa.spring.demo.util.ExtendedKeycloakContainer;
import com.github.jorgebsa.spring.demo.util.ExtendedMongoDBContainer;
import com.github.jorgebsa.spring.demo.util.ResultPage;
import com.github.jorgebsa.spring.demo.util.UserData;
import com.github.jorgebsa.spring.demo.validation.ErrorMessage;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.jorgebsa.spring.demo.util.ExtendedKeycloakContainer.CLIENT_ID;
import static com.github.jorgebsa.spring.demo.util.ExtendedKeycloakContainer.CLIENT_SECRET;
import static com.github.jorgebsa.spring.demo.util.ExtendedKeycloakContainer.REALM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
public abstract class ApplicationTests {

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = ExtendedMongoDBContainer.getInstance();

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = ExtendedKeycloakContainer.getInstance();

    public static final TypeReference<ResultPage<NoteDTO>> NOTE_RESULT_PAGE_TYPE_REFERENCE = new TypeReference<>() {
    };

    private static final Map<String, String> TOKEN_STORE = new HashMap<>();

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate template;

    @Autowired
    protected ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    protected static Stream<Arguments> userDataSource() {
        return Stream.of(
                arguments(UserData.ADMIN),
                arguments(UserData.SOME_USER)
        );
    }

    protected HttpHeaders getAuthorizationAndContentTypeHeaders(UserData userData) {
        var headers = getAuthorizationHeader(userData);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    protected HttpHeaders getAuthorizationHeader(UserData userData) {
        return getAuthorizationHeader(userData.username(), userData.password());
    }

    protected HttpHeaders getAuthorizationHeader(String username, String password) {
        var accessToken = getAccessToken(username, password);
        var httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return httpHeaders;
    }

    private String getAccessToken(String username, String password) {
        String token;
        synchronized(TOKEN_STORE) {
            token = TOKEN_STORE.computeIfAbsent(username, u -> {
                try (var keycloakClient = KeycloakBuilder.builder()
                        .serverUrl(KEYCLOAK_CONTAINER.getAuthServerUrl())
                        .realm(REALM)
                        .username(username)
                        .password(password)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .build()) {
                    return keycloakClient.tokenManager().grantToken().getToken();
                }
            });
        }
        return token;
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

    protected void assertTimestampIsRecent(String timestamp) {
        var zonedDateTime = ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        var timestampAsMillis = zonedDateTime.toInstant().toEpochMilli();

        assertMillisIsRecent(timestampAsMillis);
    }

    protected void assertMillisIsRecent(long millis) {
        var currentTimeMillis = System.currentTimeMillis();
        var start = currentTimeMillis - TimeUnit.SECONDS.toMillis(3);
        assertThat(millis).isBetween(start, currentTimeMillis);
    }

    protected String getLoginURL() {
        return "http://localhost:" + port + "/sso/login";
    }
}

