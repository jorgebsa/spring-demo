package com.example.kore.spring;

import com.example.kore.spring.base.NoteDTO;
import com.example.kore.spring.util.ExtendedMongoDBContainer;
import com.example.kore.spring.util.ResultPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

	public static final TypeReference<ResultPage<NoteDTO>> NOTE_RESULT_PAGE_TYPE_REFERENCE = new TypeReference<>() {
	};

	@Autowired
	protected TestRestTemplate template;

	@Autowired
	protected ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

}

