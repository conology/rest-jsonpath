package io.github.conology.jsonpath.mongo.spring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import io.github.conology.jsonpath.mongo.spring.model.Store;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.mongodb.core.query.Query.query;

@Testcontainers
@SpringBootTest(classes = MongoJsonPathQueryIntegrationTest.Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoJsonPathQueryIntegrationTest {

    public static final String COLLECTION_NAME = MongoJsonPathQueryIntegrationTest.class.getName();

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MongoTemplate mongoTemplate;
    @Value("classpath:stores.json")
    File storesFile;
    private MongoCollection<Document> storeCollection;


    @ParameterizedTest(name = "[{index}] {0}")
    @CsvFileSource(resources = "/MongoJsonPathQueryIntegrationTest.csv", numLinesToSkip = 1)
    void itQueries(String restQuery, String matchId, String error) {
        if (error == null) {
            if (matchId == null) {
                itQueriesEmpty(restQuery);
            } else {
                itQueriesId(restQuery, matchId);
            }
        } else {
            throw new TestAbortedException("testing of error type " + error + "not implemented");
        }
    }

    private void itQueriesEmpty(String restQuery) {
        var stores = executeQuery(restQuery);
        assertThat(stores).isEmpty();
    }

    private void itQueriesId(String restQuery, String matchId) {
        var stores = executeQuery(restQuery);
        assertThat(stores).singleElement()
            .hasFieldOrPropertyWithValue("name", matchId);
    }

    private List<Store> executeQuery(String restQuery) {
        var criteria = new JsonPathToCriteriaCompiler().compile(restQuery);

        return mongoTemplate.find(
            query(criteria),
            Store.class,
            COLLECTION_NAME
        );
    }

    @BeforeAll
    void seedCollection() throws IOException {
        var stores = objectMapper.readValue(storesFile, new TypeReference<List<Store>>() {});

        if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
            mongoTemplate.createCollection(COLLECTION_NAME);
        }
        mongoTemplate.insert(stores, COLLECTION_NAME);
    }

    @AfterAll
    void dropCollection() {
        mongoTemplate.dropCollection(COLLECTION_NAME);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    public static class Application {}
}
