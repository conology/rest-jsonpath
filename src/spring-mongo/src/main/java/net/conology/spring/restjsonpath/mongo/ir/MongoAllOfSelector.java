package net.conology.spring.restjsonpath.mongo.ir;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.HashMap;
import java.util.List;

public final class MongoAllOfSelector implements MongoSelector {

    private final List<MongoPropertyCondition> tests;

    public MongoAllOfSelector(List<MongoPropertyCondition> tests) {
        this.tests = tests;
    }

    public List<MongoPropertyCondition> getTests() {
        return tests;
    }

    public void apply(Criteria parentCritera) {
        var criteriaByPath = new HashMap<String,Criteria>();
        for (var test : tests) {
            var path = test.getPropertySelector().getPathString();
            var criteria = criteriaByPath.computeIfAbsent(
                path,
                parentCritera::and
            );
            test.getAssertion().apply(criteria);
        }
    }

    public void addTest(MongoPropertyCondition test) {
        tests.add(test);
    }

    @Override
    public Criteria asCriteria() {
        var critera = new Criteria();
        apply(critera);
        return critera;
    }
}
