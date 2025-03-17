package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public final class MongoFieldSelector implements MongoPropertySelector {

    private final List<String> path;
    private String normalizedPath;

    public MongoFieldSelector(
        List<String> path
    ) {
        this.path = path;
    }

    @Override
    public Criteria selectIn(Criteria parent) {
        var pathString = String.join(".", path);
        return parent.and(pathString);
    }

    @Override
    public String getFieldName() {
        return path.isEmpty() ? "" : path.getLast();
    }
}
