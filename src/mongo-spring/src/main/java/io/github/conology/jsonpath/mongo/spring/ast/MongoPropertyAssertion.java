package io.github.conology.jsonpath.mongo.spring.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public interface MongoPropertyAssertion {
    void accept(Criteria criteria);
}
