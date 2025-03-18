package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoElementMatch implements MongoPropertyAssertion {

    private final MongoAllOfTestNode propertyTests;

    public MongoElementMatch(MongoAllOfTestNode propertyTests) {
        this.propertyTests = propertyTests;
    }

    public void addTest(MongoPropertyTest test) {
        propertyTests.addTest(test);
    }

    @Override
    public void apply(Criteria criteria) {
        var elemMatch = new Criteria();
        propertyTests.apply(elemMatch);
        criteria.elemMatch(elemMatch);
    }
}
