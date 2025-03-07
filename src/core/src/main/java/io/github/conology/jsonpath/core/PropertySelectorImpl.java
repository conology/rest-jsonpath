package io.github.conology.jsonpath.core;

import java.util.ArrayList;
import java.util.List;

public final class PropertySelectorImpl implements PropertySelector {

    private final List<String> path = new ArrayList<>();
    private final List<CriteriaNode> criteria = new ArrayList<>();

    @Override
    public String getPath() {
        return String.join(".", path);
    }

    @Override
    public List<CriteriaNode> getCriteria() {
        return criteria;
    }

    public void addCriteria(CriteriaNode criteriaNode) {
        criteria.add(criteriaNode);
    }

    public void appendField(String field) {
        path.add(field);
    }
}
