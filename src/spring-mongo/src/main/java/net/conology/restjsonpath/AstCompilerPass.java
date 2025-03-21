package net.conology.restjsonpath;

public interface AstCompilerPass {
    net.conology.restjsonpath.core.parser.JsonPathMongoParser transformToParserRepresentation(String input);
}
