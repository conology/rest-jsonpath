package net.conology.spring.restjsonpath.mongo.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoAllOfSelector
    implements MongoAlternativesSelector, MongoSelector {

    private final List<MongoAlternativesSelector> tests;

    public MongoAllOfSelector(List<MongoAlternativesSelector> tests) {
        this.tests = tests;
    }

    public List<MongoAlternativesSelector> getTests() {
        return tests;
    }

    public void apply(Criteria parentCriteria) {
        if (tests.size() > 1) {
            applyMultipleTests(parentCriteria);
        } else {
            applySingleTest(tests.getFirst(), parentCriteria);
        }
    }

    private void applyMultipleTests(Criteria parentCriteria) {
        var criterias = new ArrayList<Criteria>();
        for (var test : tests) {
            var criteria = new Criteria();
            criterias.add(criteria);
            applySingleTest(test, criteria);
        }

        parentCriteria.andOperator(criterias);
    }

    private void applySingleTest(
        MongoAlternativesSelector test,
        Criteria parentCriteria
    ) {
        switch (test) {
            case MongoPropertyCondition propertyCondition -> {
                var path = propertyCondition
                    .getPropertySelector()
                    .getPathString();
                var criteria = parentCriteria.and(path);
                propertyCondition.getAssertion().apply(criteria);
            }
            case MongoAnyOfSelector anyOfSelector -> {
                anyOfSelector.apply(parentCriteria);
            }
            case MongoAllOfSelector allOfSelector -> {
                allOfSelector.apply(parentCriteria);
            }
        }
    }

    public void addTest(MongoAlternativesSelector test) {
        tests.add(test);
    }

    @Override
    public Criteria asCriteria() {
        var critera = new Criteria();
        apply(critera);
        return critera;
    }
}
