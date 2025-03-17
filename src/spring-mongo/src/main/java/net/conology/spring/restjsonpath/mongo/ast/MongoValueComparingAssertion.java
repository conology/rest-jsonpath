package net.conology.spring.restjsonpath.mongo.ast;

import net.conology.restjsonpath.ast.ComparisonOperator;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.function.UnaryOperator;

public class MongoValueComparingAssertion implements MongoValueAssertion {

    private final ComparisonOperator operator;
    private Object value;

    public MongoValueComparingAssertion(ComparisonOperator operator, Object value) {
        this.operator = operator;
        this.value = value;
    }

    public void updateValue(UnaryOperator<Object> updater) {
        value = updater.apply(value);
    }

    @Override
    public void apply(Criteria criteria) {
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
