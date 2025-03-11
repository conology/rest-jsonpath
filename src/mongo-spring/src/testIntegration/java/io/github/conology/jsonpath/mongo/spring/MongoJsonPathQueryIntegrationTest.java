package io.github.conology.jsonpath.mongo.spring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
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
import java.time.Instant;
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


    @ParameterizedTest(name = "[{index}] {0}: {2}")
    @CsvFileSource(resources = "/MongoJsonPathQueryIntegrationTest.csv", numLinesToSkip = 1)
    void itQueries(String ignored, String matched, String query) {
        var criteria = new JsonPathToCriteriaCompiler().compile(query);
        System.err.println(
            query + " -> " + criteria.getCriteriaObject().toJson()
        );

        var stores = mongoTemplate.find(
            query(criteria),
            Store.class,
            COLLECTION_NAME
        );

        if (matched == null) {
            assertThat(stores).isEmpty();
        } else {
            assertThat(stores).singleElement()
                .hasFieldOrPropertyWithValue("name", matched);
        }
    }

    @BeforeAll
    void seedCollection() throws IOException {
        var stores = objectMapper.readValue(storesFile, new TypeReference<List<Document>>() {});

        stores.getFirst().put("created", Instant.parse("2023-05-15T14:30:00Z"));
        stores.get(1).put("created", Instant.parse("2019-11-30T09:00:00Z"));
        stores.get(2).put("created", Instant.parse("1995-07-20T16:45:00Z"));

        if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
            mongoTemplate.createCollection(COLLECTION_NAME);
        }
        storeCollection = mongoTemplate.getCollection(COLLECTION_NAME);
        storeCollection.insertMany(stores);
    }

    @AfterAll
    void dropCollection() {
        storeCollection.drop();
    }

    public static class Store {
        String name;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    public static class Application {}
}
