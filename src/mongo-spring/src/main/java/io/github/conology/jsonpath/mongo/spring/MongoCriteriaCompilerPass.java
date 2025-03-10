package io.github.conology.jsonpath.mongo.spring;

import io.github.conology.jsonpath.core.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.NoSuchElementException;
import java.util.Objects;

public class MongoCriteriaCompilerPass {

    private final PropertyQuery ir;

    public MongoCriteriaCompilerPass(PropertyQuery ir) {
        this.ir = ir;
    }

    public static Criteria parse(String input) {
        var ir = JsonPathCompilerPass.parse(input);
        return new MongoCriteriaCompilerPass(ir).compile();
    }

    public Criteria compile() {
        return compilePropertySelector(ir);
    }

    public Criteria compilePropertySelector(PropertyQuery propertyQuery) {
        var path = normalizePath(propertyQuery);
        var criteria = new Criteria(path);
        return compilePropertySelector(criteria, propertyQuery);
    }

    private Criteria compilePropertySelector(Criteria criteria, PropertyQuery propertyQuery) {
        if (
            propertyQuery.getFilters().isEmpty()
            && propertyQuery.getChildSelector() == null
        ) {
            testExistsWithValue(criteria);
            return criteria;
        }

        applyFilters(criteria, propertyQuery);
        applyChildSelector(criteria, propertyQuery);

        return criteria;
    }

    private void applyChildSelector(Criteria criteria, PropertyQuery parentSelector) {
        if (parentSelector.getChildSelector() == null) {
            return;
        }
        var childSelector = parentSelector.getChildSelector();
        var childCriteria = criteria.and(normalizePath(childSelector));
        compilePropertySelector(childCriteria, childSelector);
    }

    private void applyFilters(Criteria criteria, PropertyQuery propertyQuery) {
        var elemMatch = new Criteria();
        for (var filter : propertyQuery.getFilters()) {
            switch (filter) {
                case ComparingFilter comparison -> applyComparison(elemMatch, comparison);
            }
        }
        criteria.elemMatch(elemMatch);
    }

    private void applyComparison(Criteria criteria, ComparingFilter comparison) {
        if (
            comparison.getLeftNode() instanceof ValueNode valueNode
                && comparison.getRightNode() instanceof PropertyQuery propertyQuery
        ) {
            applyComparison(criteria, propertyQuery, valueNode, comparison.getOperator());
        } else if (
            comparison.getLeftNode() instanceof PropertyQuery propertyQuery
                && comparison.getRightNode() instanceof ValueNode valueNode
        ) {
            applyComparison(criteria, propertyQuery, valueNode, comparison.getOperator());
        } else if (
            comparison.getLeftNode() instanceof ValueNode value1
                && comparison.getRightNode() instanceof ValueNode value2
        ) {
            if (!Objects.equals(value1.getText(), value2.getText())) {
                throw new NoSuchElementException(
                    "%s never equals %s. Therefore, this query can't yield results"
                        .formatted(
                            value1.getText(),
                            value2.getText()
                        )
                );
            }
        } else {
            throw new UnsupportedOperationException("One side of a comparison must be a literal");
        }
    }

    private void applyComparison(Criteria criteria, PropertyQuery propertyQuery, ValueNode valueNode, ComparisonOperator operator) {
        if (!propertyQuery.getFilters().isEmpty()) {
            throw new UnsupportedOperationException("Filter queries in a relative query of comparison are not supported");
        }
        if (propertyQuery.getChildSelector() != null) {
            throw new AssertionError("child selector not expected without filters");
        }
        var path = normalizePath(propertyQuery);
        applyPropertyComparison(criteria, path, valueNode, operator);
    }

    private static void testExistsWithValue(Criteria criteria) {
        criteria
            .exists(true)
            .ne(null)
            .not().size(0)
        ;
    }

    private void applyPropertyComparison(Criteria criteria, String path, ValueNode valueNode, ComparisonOperator operator) {
        switch (operator) {
            case EQ -> criteria.and(path)
                .is(valueNode.getText());
            case NEQ -> criteria.and(path)
                .ne(valueNode.getText());
            case GT -> criteria.and(path)
                .gt(valueNode.getText());
            case GTE -> criteria.and(path)
                .gte(valueNode.getText());
            case LT -> criteria.and(path)
                .lt(valueNode.getText());
            case LTE -> criteria.and(path)
                .lte(valueNode.getText());
        }
    }

    private static String normalizePath(PropertyQuery propertyQuery) {
        return String.join(".", propertyQuery.getPath());
    }
}
