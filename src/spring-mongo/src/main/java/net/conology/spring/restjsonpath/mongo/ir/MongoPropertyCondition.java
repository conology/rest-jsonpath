package net.conology.spring.restjsonpath.mongo.ir;

import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoPropertyCondition implements MongoSelector, MongoAlternativesSelector {

    private final MongoFieldSelector propertySelector;
    private final MongoPropertyAssertion assertion;

    public MongoPropertyCondition(
            MongoFieldSelector propertySelector,
            MongoPropertyAssertion assertion) {
        this.propertySelector = propertySelector;
        this.assertion = assertion;
    }

    public MongoFieldSelector getPropertySelector() {
        return propertySelector;
    }

    public MongoPropertyAssertion getAssertion() {
        return assertion;
    }

    @Override
    public Criteria asCriteria() {
        var criteria = new Criteria(propertySelector.getPathString());
        assertion.apply(criteria);
        return criteria;
    }
}
