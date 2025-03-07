package io.github.conology.jsonpath.core;

import io.github.goatfryed.json_path_mongo.JsonPathMongoParser;

public class FilterQuery {

    private final JsonPathMongoParser.MongoQueryContext inner;

    public FilterQuery(JsonPathMongoParser.MongoQueryContext inner) {
        this.inner = inner;
    }

    public JsonPathMongoParser.MongoQueryContext getInner() {
        return inner;
    }
}
