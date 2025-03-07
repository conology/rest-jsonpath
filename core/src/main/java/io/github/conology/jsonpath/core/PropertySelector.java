package io.github.conology.jsonpath.core;

import java.util.List;

public non-sealed interface PropertySelector extends CriteriaNode, ExpressionNode {
    String getPath();

    List<CriteriaNode> getCriteria();
}
