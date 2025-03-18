package net.conology.spring.restjsonpath.mongo.postprocessor;

import net.conology.restjsonpath.PostProcessor;
import net.conology.spring.restjsonpath.mongo.ir.MongoAllOfSelector;
import net.conology.spring.restjsonpath.mongo.ir.MongoPropertyCondition;
import net.conology.spring.restjsonpath.mongo.ir.MongoSelector;

public abstract class AbstractMongoTestPostProcessor implements PostProcessor<MongoSelector> {

    @Override
    public void accept(MongoSelector ast) {
        switch (ast) {
            case MongoAllOfSelector allOfNode ->
                allOfNode.getTests().forEach(this::accept);
            case MongoPropertyCondition testNode -> accept(testNode);
        }
    }

    public abstract void accept(MongoPropertyCondition test);
}
