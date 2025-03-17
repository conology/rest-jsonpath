package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public interface MongoPropertyAssertion {
    void apply(Criteria criteria);
}
