package io.github.conology.jsonpath.mongo.spring.ast;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public final class MongoElementMatch implements MongoPropertyAssertion {

    private final List<MongoTestNode> propertyTests;

    public MongoElementMatch(List<MongoTestNode> propertyTests) {
        this.propertyTests = propertyTests;
    }

    public void addTest(MongoPropertyTest test) {
        propertyTests.add(test);
    }

    @Override
    public void accept(Criteria criteria) {
        var elemMatch = new Criteria();
        for (MongoTestNode propertyTest : propertyTests) {
            propertyTest.visit(elemMatch);
        }
        criteria.elemMatch(elemMatch);
    }
}
