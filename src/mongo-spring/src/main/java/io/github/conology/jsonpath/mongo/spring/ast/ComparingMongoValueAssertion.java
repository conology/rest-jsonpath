package io.github.conology.jsonpath.mongo.spring.ast;

import io.github.conology.jsonpath.core.ast.ComparisonOperator;
import org.springframework.data.mongodb.core.query.Criteria;

public class ComparingMongoValueAssertion implements MongoValueAssertion {

    private final ComparisonOperator operator;
    private final Object value;

    public ComparingMongoValueAssertion(ComparisonOperator operator, Object value) {
        this.operator = operator;
        this.value = value;
    }

    @Override
    public void accept(Criteria criteria) {
        switch (operator) {
            case EQ -> criteria.is(value);
            case NEQ -> criteria.ne(value);
            case GT -> criteria.gt(value);
            case GTE -> criteria.gte(value);
            case LT -> criteria.lt(value);
            case LTE -> criteria.lte(value);
        }
    }
}
