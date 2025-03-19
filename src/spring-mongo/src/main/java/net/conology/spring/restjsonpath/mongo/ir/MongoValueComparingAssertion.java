package net.conology.spring.restjsonpath.mongo.ir;

import net.conology.restjsonpath.InvalidQueryException;
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
        guardPrimitiveComparison();

        switch (operator) {
            case EQ -> criteria.is(value);
            case NEQ -> criteria.ne(value);
            case GT -> criteria.gt(value);
            case GTE -> criteria.gte(value);
            case LT -> criteria.lt(value);
            case LTE -> criteria.lte(value);
        }
    }

    private void guardPrimitiveComparison() {
        switch (operator) {
            case EQ, NEQ -> {/* cool */}
            case GT, GTE, LT, LTE -> {
                // note that we only validate primitive types that we handle
                // library consumers might define own types that may or may not support this operator
                // In that case, validation should be handled in postprocessors anyway
                if (value instanceof Boolean) {
                    throw new InvalidQueryException(
                        "Can't apply operator %s to value of type %s"
                            .formatted(operator, value.getClass().getSimpleName())
                    );
                }
            }
        }
    }
}
