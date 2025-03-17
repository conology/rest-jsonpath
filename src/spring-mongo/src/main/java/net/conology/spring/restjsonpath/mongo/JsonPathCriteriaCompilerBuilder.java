package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.IrVisitor;
import net.conology.spring.restjsonpath.mongo.ast.MongoDelegatingValueAssertion;
import net.conology.spring.restjsonpath.mongo.ast.MongoTestNode;
import net.conology.spring.restjsonpath.mongo.ast.MongoValueAssertion;
import net.conology.restjsonpath.core.parser.JsonPathMongoParser;
import org.antlr.v4.runtime.BailErrorStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class JsonPathCriteriaCompilerBuilder {

    private MongoValueAssertion existenceAssertion;
    private List<IrVisitor<MongoTestNode>> mongoIrVisitors;

    private Consumer<JsonPathMongoParser> parserConfigurer = parser -> parser.setErrorHandler(new BailErrorStrategy());

    public JsonPathCriteriaCompilerBuilder parserConfigurer(Consumer<JsonPathMongoParser> parserConfigurer) {
        this.parserConfigurer = parserConfigurer;
        return this;
    }

    public JsonPathCriteriaCompilerBuilder existenceAssertion(MongoValueAssertion existenceAssertion) {
        this.existenceAssertion = existenceAssertion;
        return this;
    }

    public JsonPathCriteriaCompilerBuilder addMongoTestNodeVisitor(IrVisitor<MongoTestNode> testVisitor) {
        if (mongoIrVisitors == null) {
            mongoIrVisitors = new ArrayList<>();
        }
        mongoIrVisitors.add(testVisitor);
        return this;
    }

    public JsonPathCriteriaCompiler build() {
        withDefaults();

        var mongoIrCompilerPassBuilder
            = new MongoIrCompilerPass.Builder()
                .existenceAssertion(existenceAssertion);

        return new JsonPathCriteriaCompiler(
            parserConfigurer, mongoIrCompilerPassBuilder,
            mongoIrVisitors
        );
    }

    private void withDefaults() {
        if (existenceAssertion == null) {
            existenceAssertion = MongoDelegatingValueAssertion.createDefaultExistenceAssertion();
        }
        if (parserConfigurer == null) {
            parserConfigurer = parser -> {
              parser.setErrorHandler(new BailErrorStrategy());
            };
        }
        if (mongoIrVisitors == null) {
            mongoIrVisitors = Collections.emptyList();
        }
    }
}