package io.github.conology.jsonpath.mongo.spring;

import io.github.conology.jsonpath.core.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class MongoCriteriaCompilerPass {

    private final QueryNode ir;

    public MongoCriteriaCompilerPass(QueryNode ir) {
        this.ir = ir;
    }

    public static Criteria parse(String input) {
        var ir = JsonPathCompilerPass.parse(input);
        return new MongoCriteriaCompilerPass(ir).compile();
    }

    public Criteria compile() {
        return compile(ir);
    }

    private Criteria compile(QueryNode ir) {
        return switch (ir) {
            case ComparingQuery comparingQuery -> compileRootPropertyTest(comparingQuery);
            case PropertyQuery propertyQuery -> compileRootPropertyTest(propertyQuery);
        };
    }

    private Criteria compileRootPropertyTest(ComparingQuery comparingQuery) {
        return compileComparisonTest(comparingQuery)
            .map(this::compileRootPropertyTest)
            .orElse(new Criteria());
    }

    private Criteria compileRootPropertyTest(MongoPropertyTest test) {
        var path = normalizePath(test.propertyQuery());
        var criteria = new Criteria(path);
        return compilePropertyTest(criteria, test);
    }

    public Criteria compileRootPropertyTest(PropertyQuery propertyQuery) {
        var path = normalizePath(propertyQuery);
        var criteria = new Criteria(path);
        var test = new MongoPropertyTest(propertyQuery, MongoCriteriaCompilerPass::testExistsWithValue);
        return compilePropertyTest(criteria, test);
    }

    private void applyChildPropertyTest(Criteria elemMatch, PropertyQuery propertyQuery) {
        var test = new MongoPropertyTest(propertyQuery, MongoCriteriaCompilerPass::testExistsWithValue);
        applyChildPropertyTest(elemMatch, test);
    }

    private void applyChildPropertyTest(Criteria criteria, MongoPropertyTest test) {
        var path = normalizePath(test.propertyQuery());
        compilePropertyTest(criteria.and(path), test);
    }

    private Criteria compilePropertyTest(Criteria criteria, MongoPropertyTest test) {
        var propertyQuery = test.propertyQuery();

        if (!propertyQuery.getFilters().isEmpty()) {
            var elemMatch = compileElementMatch(propertyQuery.getFilters());
            criteria.elemMatch(elemMatch);
        } else if (propertyQuery.getChildSelector() == null) {
            test.testStrategy().accept(criteria);
        }

        if (propertyQuery.getChildSelector() != null) {
            applyChildPropertyTest(criteria, test.propertyQuery(propertyQuery.getChildSelector()));
        }

        return criteria;
    }

    private Criteria compileElementMatch(List<QueryNode> queries) {
        var elemMatch = new Criteria();
        for (var filter : queries) {
            switch (filter) {
                case ComparingQuery comparison -> compileComparisonTest(comparison)
                    .ifPresent(test -> applyChildPropertyTest(elemMatch, test));
                case PropertyQuery propertyQuery -> applyChildPropertyTest(elemMatch, propertyQuery);
            }
        }
        return elemMatch;
    }

    private Optional<MongoPropertyTest> compileComparisonTest(ComparingQuery comparison) {
        switch (comparison.getLeftNode()) {
            case PropertyQuery propertyQuery when comparison.getRightNode() instanceof ValueNode valueNode -> {
                return Optional.of(compileComparisonTest(propertyQuery, valueNode, comparison.getOperator()));
            }
            case ValueNode valueNode when comparison.getRightNode() instanceof PropertyQuery propertyQuery -> {
                return Optional.of(compileComparisonTest(propertyQuery, valueNode, comparison.getOperator()));
            }
            case ValueNode value1 when comparison.getRightNode() instanceof ValueNode value2 -> {
                if (!Objects.equals(value1.getText(), value2.getText())) {
                    throw new NoSuchElementException(
                        "%s never equals %s. Therefore, this query can't yield results"
                            .formatted(
                                value1.getText(),
                                value2.getText()
                            )
                    );
                }
                return Optional.empty();
            }
            case null, default -> throw new UnsupportedOperationException("One side of a comparison must be a literal");
        }
    }

    private MongoPropertyTest compileComparisonTest(
        PropertyQuery propertyQuery,
        ValueNode valueNode,
        ComparisonOperator operator
    ) {
        return new MongoPropertyTest(
            propertyQuery,
            criteria -> applyPropertyComparison(criteria, valueNode, operator)
        );
    }

    private static void testExistsWithValue(Criteria criteria) {
        criteria
            .exists(true)
            .ne(null)
            .not().size(0)
        ;
    }

    private void applyPropertyComparison(Criteria criteria, ValueNode valueNode, ComparisonOperator operator) {
        switch (operator) {
            case EQ -> criteria.is(valueNode.getText());
            case NEQ -> criteria.ne(valueNode.getText());
            case GT -> criteria.gt(valueNode.getText());
            case GTE -> criteria.gte(valueNode.getText());
            case LT -> criteria.lt(valueNode.getText());
            case LTE -> criteria.lte(valueNode.getText());
        }
    }

    private static String normalizePath(PropertyQuery propertyQuery) {
        return String.join(".", propertyQuery.getPath());
    }
}
