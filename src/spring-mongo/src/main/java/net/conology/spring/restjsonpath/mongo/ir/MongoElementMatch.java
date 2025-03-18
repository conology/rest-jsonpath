package net.conology.spring.restjsonpath.mongo.ir;

import org.springframework.data.mongodb.core.query.Criteria;

public final class MongoElementMatch implements MongoPropertyAssertion {

    private final MongoAllOfSelector propertyTests;

    public MongoElementMatch(MongoAllOfSelector propertyTests) {
        this.propertyTests = propertyTests;
    }

    public void addTest(MongoPropertyCondition test) {
        propertyTests.addTest(test);
    }

    @Override
    public void apply(Criteria criteria) {
        var elemMatch = new Criteria();
        propertyTests.apply(elemMatch);
        criteria.elemMatch(elemMatch);
    }
}
