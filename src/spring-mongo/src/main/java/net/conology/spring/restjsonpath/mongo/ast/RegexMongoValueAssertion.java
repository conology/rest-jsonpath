package net.conology.spring.restjsonpath.mongo.ast;

import net.conology.restjsonpath.ast.RegexFilterNode;
import org.springframework.data.mongodb.core.query.Criteria;

public class RegexMongoValueAssertion implements MongoPropertyAssertion {
    private final RegexFilterNode node;

    public RegexMongoValueAssertion(RegexFilterNode node) {
        this.node = node;
    }


    @Override
    public void accept(Criteria criteria) {
        var sb = new StringBuilder();
        node.getOptions().forEach(sb::append);
        var optionsString = sb.toString();

        if (node.isNegated()) {
            criteria.not()
                .regex(node.getRegexPattern(), optionsString.isBlank() ? null : optionsString);
        } else {
            criteria.regex(node.getRegexPattern(), optionsString.isBlank() ? null : optionsString);
        }
    }
}
