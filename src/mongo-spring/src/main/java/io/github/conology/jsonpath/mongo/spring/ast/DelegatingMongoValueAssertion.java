package io.github.conology.jsonpath.mongo.spring.ast;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.function.Consumer;

public class DelegatingMongoValueAssertion implements MongoValueAssertion {

    private final Consumer<Criteria> strategy;

    public DelegatingMongoValueAssertion(Consumer<Criteria> existenceCriteriaStrategy) {
        this.strategy = existenceCriteriaStrategy;
    }

    @Override
    public void accept(Criteria criteria) {
        strategy.accept(criteria);
    }

    public static MongoValueAssertion createDefaultExistenceAssertion() {
        return new DelegatingMongoValueAssertion(
            criteria -> criteria
                .exists(true)
                .ne(null)
                .not().size(0)
        );
    }
}
