package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.HashMap;
import java.util.List;

public final class MongoAllOfTestNode implements MongoTestNode {

    private final List<MongoPropertyTest> tests;

    public MongoAllOfTestNode(List<MongoPropertyTest> tests) {
        this.tests = tests;
    }

    public List<MongoPropertyTest> getTests() {
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

    public void addTest(MongoPropertyTest test) {
        tests.add(test);
    }

    @Override
    public Criteria asCriteria() {
        var critera = new Criteria();
        apply(critera);
        return critera;
    }
}
