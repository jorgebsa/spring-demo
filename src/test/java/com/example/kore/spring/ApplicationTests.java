package com.example.kore.spring;

import com.example.kore.spring.util.ExtendedMongoDBContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
public class ApplicationTests {

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = ExtendedMongoDBContainer.getInstance();

    @Test
    void contextLoads() {
    }

}

