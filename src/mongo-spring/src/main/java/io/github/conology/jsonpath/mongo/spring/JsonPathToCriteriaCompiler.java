package io.github.conology.jsonpath.mongo.spring;

import io.github.conology.jsonpath.core.JsonPathCompilerPass;
import io.github.conology.jsonpath.mongo.spring.ast.DelegatingMongoValueAssertion;
import org.springframework.data.mongodb.core.query.Criteria;

public class JsonPathToCriteriaCompiler {

    private final MongoAstCompilerPass.Builder mongoAstCompilerPassBuilder
        = new MongoAstCompilerPass.Builder()
            .existenceAssertion(DelegatingMongoValueAssertion.createDefaultExistenceAssertion());

    public Criteria compile(String input) {

        var jsonPathIr = JsonPathCompilerPass.parseRestQuery(input);

        var mongoIr = mongoAstCompilerPassBuilder
            .build(jsonPathIr).compile();

        var critera = new Criteria();
        mongoIr.accept(critera);

        return critera;
    }
}
