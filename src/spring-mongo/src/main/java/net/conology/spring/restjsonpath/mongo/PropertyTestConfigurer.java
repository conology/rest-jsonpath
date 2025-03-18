package net.conology.spring.restjsonpath.mongo;

import net.conology.spring.restjsonpath.mongo.ir.MongoPropertyCondition;
import net.conology.spring.restjsonpath.mongo.ir.MongoValueComparingAssertion;
import net.conology.spring.restjsonpath.mongo.postprocessor.AbstractMongoTestPostProcessor;

public abstract class PropertyTestConfigurer extends AbstractMongoTestPostProcessor {

    @Override
    public void accept(MongoPropertyCondition test) {
        if (handles(test)) return;

        configure(test);
    }

    private void configure(MongoPropertyCondition test) {
        if (test.getAssertion() instanceof MongoValueComparingAssertion valueComparison) {
            accept(valueComparison);
        }
    }

    protected abstract boolean handles(MongoPropertyCondition test);

    protected abstract void accept(MongoValueComparingAssertion valueComparison);
}
