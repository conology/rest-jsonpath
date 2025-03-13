package io.github.conology.jsonpath.mongo.spring;

import io.github.conology.jsonpath.core.JsonPathCompilerPass;
import io.github.conology.jsonpath.mongo.spring.ast.MongoDelegatingValueAssertion;
import org.springframework.data.mongodb.core.query.Criteria;

public class JsonPathToCriteriaCompiler {

    private final MongoAstCompilerPass.Builder mongoAstCompilerPassBuilder
        = new MongoAstCompilerPass.Builder()
            .existenceAssertion(MongoDelegatingValueAssertion.createDefaultExistenceAssertion());

    public Criteria compile(String input) {

        var jsonPathIr = JsonPathCompilerPass.parseRestQuery(input);

        var mongoIr = mongoAstCompilerPassBuilder
            .build(jsonPathIr).compile();

        var critera = new Criteria();
        mongoIr.visit(critera);

        return critera;
    }
}
