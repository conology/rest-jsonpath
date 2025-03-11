package io.github.conology.jsonpath.mongo.spring.ast;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public final class MongoElementMatch implements MongoPropertyAssertion {

    private final List<MongoPropertyTest> propertyTests;

    public MongoElementMatch(List<MongoPropertyTest> propertyTests) {
        this.propertyTests = propertyTests;
    }

    public void addTest(MongoPropertyTest test) {
        propertyTests.add(test);
    }

    @Override
    public void accept(Criteria criteria) {
        var elemMatch = new Criteria();
        for (MongoPropertyTest propertyTest : propertyTests) {
            propertyTest.accept(elemMatch);
        }
        criteria.elemMatch(elemMatch);
    }
}
