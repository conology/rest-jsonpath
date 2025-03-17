package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoPropertyTest implements MongoTestNode {

    private final MongoPropertySelector propertySelector;
    private final MongoPropertyAssertion assertion;

    public MongoPropertyTest(
        MongoPropertySelector propertySelector,
        MongoPropertyAssertion assertion
    ) {
        this.propertySelector = propertySelector;
        this.assertion = assertion;
    }

    public MongoPropertySelector getPropertySelector() {
        return propertySelector;
    }

    public MongoPropertyAssertion getAssertion() {
        return assertion;
    }

    @Override
    public void visit(Criteria parentCriteria) {
        var childCritera = propertySelector.selectIn(parentCriteria);
        assertion.apply(childCritera);
    }
}
