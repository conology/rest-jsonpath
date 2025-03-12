package io.github.conology.jsonpath.mongo.spring;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.opentest4j.TestAbortedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoCriteriaCompilerPassTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvFileSource(resources = "/MongoCriteriaCompilerPassTest.csv", numLinesToSkip = 1)
    void test(String input, String mongoQuery) {
        switch (mongoQuery) {
            case "!unsupported" -> throw new TestAbortedException("error testing not implemented");
            case null -> throw new TestAbortedException("behavior not yet defined");
            default -> testTranslatablePath(input, mongoQuery);
        }
    }

    private void testTranslatablePath(String input, String mongoQuery) {
        var criteria = new JsonPathToCriteriaCompiler().compile(input);
        assertEquals(mongoQuery, criteria.getCriteriaObject().toJson());
    }
}