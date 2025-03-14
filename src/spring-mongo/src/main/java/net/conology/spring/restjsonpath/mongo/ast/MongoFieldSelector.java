package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoFieldSelector implements MongoPropertySelector {

    private final String path;

    public MongoFieldSelector(String path) {
        this.path = path;
    }

    @Override
    public Criteria selectIn(Criteria parent) {
        return parent.and(path);
    }
}
