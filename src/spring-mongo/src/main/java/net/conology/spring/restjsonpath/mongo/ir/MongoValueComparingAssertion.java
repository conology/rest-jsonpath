package net.conology.spring.restjsonpath.mongo.ir;

import net.conology.restjsonpath.InvalidQueryException;
import net.conology.restjsonpath.ast.ComparisonOperator;
import net.conology.restjsonpath.ast.ValueNode;
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
        switch (value) {
            case ValueNode.SPECIAL_VALUE.NULL -> applyUnorderedComparison(criteria, null);
            case Boolean ignored -> applyUnorderedComparison(criteria, ignored);
            default -> applyStandardComparison(criteria);
        }
    }

    private void applyStandardComparison(Criteria criteria) {
        switch (operator) {
            case EQ -> criteria.is(value);
            case NEQ -> criteria.ne(value);
            case GT -> criteria.gt(value);
            case GTE -> criteria.gte(value);
            case LT -> criteria.lt(value);
            case LTE -> criteria.lte(value);
        }
    }

    private void applyUnorderedComparison(Criteria criteria, Object object) {
        switch (operator) {
            case EQ -> criteria.is(object);
            case NEQ -> criteria.ne(object);
            case GT, GTE, LT, LTE -> throw failOperatorNotApplicable();
        }
    }

    private InvalidQueryException failOperatorNotApplicable() {
        return new InvalidQueryException(
            "Can't apply operator %s to value of type %s"
                .formatted(operator, value.getClass().getSimpleName())
        );
    }
}
