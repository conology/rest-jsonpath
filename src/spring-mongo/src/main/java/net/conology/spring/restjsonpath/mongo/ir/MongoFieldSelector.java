package net.conology.spring.restjsonpath.mongo.ir;

import java.util.List;

public final class MongoFieldSelector {

    private List<String> path;

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

    public List<String> getPath() {
        return path;
    }

    public MongoFieldSelector setPath(List<String> path) {
        this.path = path;
        return this;
    }
}
