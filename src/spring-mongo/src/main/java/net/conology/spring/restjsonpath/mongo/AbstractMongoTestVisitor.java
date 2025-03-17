package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.IrVisitor;
import net.conology.spring.restjsonpath.mongo.ast.MongoAllOfTestNode;
import net.conology.spring.restjsonpath.mongo.ast.MongoPropertyTest;
import net.conology.spring.restjsonpath.mongo.ast.MongoTestNode;

abstract public class AbstractMongoTestVisitor implements IrVisitor<MongoTestNode> {

    @Override
    public void accept(MongoTestNode ast) {
        switch (ast) {
            case MongoAllOfTestNode allOfNode ->
                allOfNode.getTests().forEach(this::accept);
            case MongoPropertyTest testNode -> accept(testNode);
        }
    }

    abstract public void accept(MongoPropertyTest test);
}
