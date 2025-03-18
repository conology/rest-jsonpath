package net.conology.spring.restjsonpath.mongo.ast;

import org.springframework.data.mongodb.core.query.Criteria;

public sealed interface MongoTestNode
    permits MongoAllOfTestNode, MongoPropertyTest {
    Criteria asCriteria();
}
