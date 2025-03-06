package io.github.goatfryed.json_path_mongo.spring_mongo;

import io.github.goatfryed.json_path_mongo.parser.JsonPathCompilerPass;
import io.github.goatfryed.json_path_mongo.parser.PropertyFilter;
import io.github.goatfryed.json_path_mongo.parser.PropertySelector;
import org.springframework.data.mongodb.core.query.Criteria;

public class MongoCriteriaCompilerPass {

    public static Criteria parse(String input) {
        var ir = JsonPathCompilerPass.parse(input);
        return new MongoCriteriaCompilerPass().compile(ir);
    }

    public Criteria compile(PropertySelector propertySelector) {
        var criteria = new Criteria();
        visit(criteria, propertySelector);
        return criteria;
    }

    private void visit(Criteria criteria, PropertySelector propertySelector) {
        var propertyCriterion = criteria.and(propertySelector.getPath());
        if (propertySelector.getCriteria().isEmpty()) {
            propertyCriterion.exists(true);
            return;
        }

        var elementMatch = new Criteria();
        for (var criterion : propertySelector.getCriteria()) {
            switch (criterion) {
                case PropertySelector it -> visit(elementMatch, it);
                case PropertyFilter it -> visit(elementMatch, it);
            }
        }
        propertyCriterion.elemMatch(elementMatch);
    }

    private void visit(Criteria criteria, PropertyFilter propertyFilter) {
        switch (propertyFilter.getOperator()) {
            case EQ -> criteria.and(propertyFilter.getPropertyNode().getPath())
                .is(propertyFilter.getValueNode().getText());
            case NEQ -> criteria.and(propertyFilter.getPropertyNode().getPath())
                .ne(propertyFilter.getValueNode().getText());
            case GT -> criteria.and(propertyFilter.getPropertyNode().getPath())
                .gt(propertyFilter.getValueNode().getText());
            case GTE -> criteria.and(propertyFilter.getPropertyNode().getPath())
                .gte(propertyFilter.getValueNode().getText());
            case LT -> criteria.and(propertyFilter.getPropertyNode().getPath())
                .lt(propertyFilter.getValueNode().getText());
            case LTE -> criteria.and(propertyFilter.getPropertyNode().getPath())
                .lte(propertyFilter.getValueNode().getText());
        };
    }
}
