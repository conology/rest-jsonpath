package net.conology.spring.restjsonpath.mongo.ir;

public sealed interface MongoAlternativesSelector
    extends MongoSelector
    permits MongoAllOfSelector, MongoAnyOfSelector, MongoPropertyCondition {}
