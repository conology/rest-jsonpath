package net.conology.spring.restjsonpath.mongo;

import net.conology.spring.restjsonpath.mongo.ast.MongoPropertyTest;
import net.conology.spring.restjsonpath.mongo.ast.MongoValueComparingAssertion;

public abstract class PropertyTestConfigurer extends AbstractMongoTestVisitor {
    @Override
    public void accept(MongoPropertyTest test) {
        if (isHandledField(test)) return;

        configure(test);
    }

    private void configure(MongoPropertyTest test) {
        if (test.getAssertion() instanceof MongoValueComparingAssertion valueComparison) {
            accept(valueComparison);
        }
    }

    protected abstract boolean isHandledField(MongoPropertyTest test);

    protected abstract void accept(MongoValueComparingAssertion valueComparison);
}
