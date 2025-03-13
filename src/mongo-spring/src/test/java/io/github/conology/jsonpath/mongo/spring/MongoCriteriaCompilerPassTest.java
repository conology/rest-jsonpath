package io.github.conology.jsonpath.mongo.spring;

import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.opentest4j.TestAbortedException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoCriteriaCompilerPassTest {

    private static final Pattern ERROR_EXPECTATION_PATTERN = Pattern.compile("^!([A-z]+)(:.*)?$");

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvFileSource(resources = "/MongoCriteriaCompilerPassTest.csv", numLinesToSkip = 1)
    void test(String input, String expectation) {
        if (expectation == null) {
            throw new TestAbortedException("behavior not yet defined");
        }

        var errorExpectation = ERROR_EXPECTATION_PATTERN.matcher(expectation);
        if (errorExpectation.matches()) {
            testException(input, errorExpectation.group(1), errorExpectation.group(2));
        } else {
            testTranslatablePath(input, expectation);
        }
    }

    private void testException(String input, String errorType, String errorMsg) {
        if ("error".equals(errorType)) {
            var thatActual = assertThatCode(() -> compile(input))
                .describedAs("compilation error")
                .isNotNull();
            if (errorMsg != null) {
                thatActual.hasMessageContaining(errorMsg);
            }
        } else {
            throw new TestInstantiationException("error test of type %s not defined".formatted(errorType));
        }
    }

    private void testTranslatablePath(String input, String mongoQuery) {
        var criteria = compile(input);
        assertEquals(mongoQuery, criteria.getCriteriaObject().toJson());
    }

    private static Criteria compile(String input) {
        return new JsonPathToCriteriaCompiler().compile(input);
    }
}