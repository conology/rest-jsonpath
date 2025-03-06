package io.github.goatfryed.json_path_mongo.parser;

import java.util.List;

public non-sealed interface PropertySelector extends CriteriaNode, ExpressionNode {
    String getPath();

    List<CriteriaNode> getCriteria();
}
