package net.conology.spring.restjsonpath.mongo.ir;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.HashMap;
import java.util.List;

public final class MongoAllOfSelector implements MongoSelector {

    private final List<MongoAlternativesSelector> tests;

    public MongoAllOfSelector(List<MongoAlternativesSelector> tests) {
        this.tests = tests;
    }

    public List<MongoAlternativesSelector> getTests() {
        return tests;
    }

    public void apply(Criteria parentCritera) {
        var criteriaByPath = new HashMap<String, Criteria>();
        for (var test : tests) {
            switch (test) {
                case MongoPropertyCondition propertyCondition -> {
                    var path = propertyCondition.getPropertySelector().getPathString();
                    var criteria = criteriaByPath.computeIfAbsent(
                            path,
                            parentCritera::and);
                    propertyCondition.getAssertion().apply(criteria);
                }
                case MongoAnyOfSelector anyOfSelector -> anyOfSelector.apply(parentCritera);
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
