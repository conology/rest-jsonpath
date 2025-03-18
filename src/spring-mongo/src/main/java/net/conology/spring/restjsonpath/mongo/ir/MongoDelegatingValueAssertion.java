package net.conology.spring.restjsonpath.mongo.ir;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.function.Consumer;

public class MongoDelegatingValueAssertion implements MongoValueAssertion {

    private final Consumer<Criteria> strategy;

    public MongoDelegatingValueAssertion(Consumer<Criteria> existenceCriteriaStrategy) {
        this.strategy = existenceCriteriaStrategy;
    }

    @Override
    public void apply(Criteria criteria) {
        strategy.accept(criteria);
    }

    public static MongoValueAssertion createDefaultExistenceAssertion() {
        return new MongoDelegatingValueAssertion(
            criteria -> criteria
                .exists(true)
                .ne(null)
                .not().size(0)
        );
    }
}
