package io.github.conology.jsonpath.mongo.spring.ast;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Set;

public class RegexMongoValueAssertion implements MongoPropertyAssertion {
    private final String regexPattern;
    private final Set<Character> options;

    public RegexMongoValueAssertion(String regexPattern, Set<Character> options) {
        this.regexPattern = regexPattern;
        this.options = options;
    }


    @Override
    public void accept(Criteria criteria) {
        var sb = new StringBuilder();
        options.forEach(sb::append);
        var optionsString = sb.toString();
        criteria.regex(regexPattern, optionsString.isBlank() ? null : optionsString);
    }
}
