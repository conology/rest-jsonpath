package io.github.conology.jsonpath.mongo.spring.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoPropertyTest implements MongoTestNode {

    private final MongoPropertySelector propertySelector;
    private final MongoPropertyAssertion assertion;

    public MongoPropertyTest(MongoPropertySelector propertySelector, MongoPropertyAssertion assertion) {
        this.propertySelector = propertySelector;
        this.assertion = assertion;
    }

    @Override
    public void visit(Criteria parentCritera) {
        var childCritera = propertySelector.selectIn(parentCritera);
        assertion.accept(childCritera);
    }
}
