package io.github.conology.jsonpath.mongo.spring.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public interface MongoPropertySelector {

    Criteria selectIn(Criteria parent);
}
