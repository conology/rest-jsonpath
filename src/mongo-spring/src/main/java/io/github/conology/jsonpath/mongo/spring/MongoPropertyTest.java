package io.github.conology.jsonpath.mongo.spring;

import io.github.conology.jsonpath.core.PropertyQuery;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.function.Consumer;

public record MongoPropertyTest(
    PropertyQuery propertyQuery,
    Consumer<Criteria> testStrategy
) {
    public MongoPropertyTest propertyQuery(PropertyQuery propertyQuery) {
        return new MongoPropertyTest(propertyQuery, testStrategy());
    }
}
