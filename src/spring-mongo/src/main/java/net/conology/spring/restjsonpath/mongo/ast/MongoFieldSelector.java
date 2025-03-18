package net.conology.spring.restjsonpath.mongo.ast;

import java.util.List;

public final class MongoFieldSelector {

    private final List<String> path;

    public MongoFieldSelector(
        List<String> path
    ) {
        this.path = path;
    }

    public String getPathString() {
        return String.join(".", path);
    }

    public String getFieldName() {
        return path.isEmpty() ? "" : path.getLast();
    }
}
