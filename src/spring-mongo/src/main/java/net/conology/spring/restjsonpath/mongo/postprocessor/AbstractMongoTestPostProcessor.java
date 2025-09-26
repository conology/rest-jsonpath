package net.conology.spring.restjsonpath.mongo.postprocessor;

import net.conology.restjsonpath.PostProcessor;
import net.conology.spring.restjsonpath.mongo.ir.*;

public abstract class AbstractMongoTestPostProcessor implements PostProcessor<MongoSelector> {

    @Override
    public void accept(MongoSelector ast) {
        switch (ast) {
            case MongoAllOfSelector allOfNode ->
                allOfNode.getTests().forEach(this::accept);
            case MongoAnyOfSelector anyOfNode ->
                anyOfNode.getAllOfSelectors().forEach(this::accept);
            case MongoPropertyCondition testNode -> {
                accept(testNode);
                if(testNode.getAssertion() instanceof MongoElementMatch elemMatch) {
                    accept(elemMatch.getTests());
                }
            }
        }
    }

    public abstract void accept(MongoPropertyCondition test);
}
