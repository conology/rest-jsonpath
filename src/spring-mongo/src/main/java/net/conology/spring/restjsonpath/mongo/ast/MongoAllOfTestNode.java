package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public final class MongoAllOfTestNode implements MongoTestNode {

    private final List<MongoPropertyTest> tests;

    public MongoAllOfTestNode(List<MongoPropertyTest> tests) {
        this.tests = tests;
    }

    public List<MongoPropertyTest> getTests() {
        return tests;
    }

    @Override
    public void visit(Criteria parentCritera) {
        tests.forEach(test -> test.visit(parentCritera));
    }
}
