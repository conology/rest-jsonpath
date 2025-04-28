package net.conology.spring.restjsonpath.mongo.ir;

import org.springframework.data.mongodb.core.query.Criteria;

public sealed interface MongoSelector
    permits MongoAllOfSelector, MongoPropertyCondition, MongoAnyOfSelector, MongoAlternativesSelector {

    Criteria asCriteria();
}
