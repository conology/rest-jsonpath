package net.conology.spring.restjsonpath.mongo;

import net.conology.restjsonpath.InvalidQueryException;
import net.conology.spring.restjsonpath.mongo.postprocessor.SimpleDateTimePropertyMapper;
import net.conology.spring.restjsonpath.mongo.postprocessor.SimpleFieldNameMapper;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.opentest4j.TestAbortedException;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoCriteriaCompilerPassTest {

    private static final Pattern ERROR_EXPECTATION_PATTERN = Pattern.compile("^!([A-z]+)(?::(.*))?$");

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
        if ("lowPriority".equals(errorType)) {
            // we don't care if it works or not
            // no guarantees given
            throw new TestAbortedException("behavior not enforced");
        }

        if ("undefined".equals(errorType)) {
            testTranslatablePath(input, errorMsg);
            return;
        }

        var compilationError = assertThatCode(() -> compile(input))
                .describedAs("compilation error");

        if ("error".equals(errorType)) {
            compilationError.isInstanceOf(Exception.class);
        } else if ("unsupported".equals(errorType)
                || "invalidQuery".equals(errorType)) {
            compilationError.isInstanceOf(InvalidQueryException.class);
        } else {
            throw new TestInstantiationException("error test of type %s not defined".formatted(errorType));
        }

        if (errorMsg != null) {
            compilationError.hasMessageContaining(errorMsg);
        }
    }

    private void testTranslatablePath(String input, String mongoQuery) {
        var criteria = compile(input);
        assertEquals(mongoQuery, criteria.getCriteriaObject().toJson());
    }

    private static Criteria compile(String input) {
        return new JsonPathCriteriaCompilerBuilder()
                .mongoSelectorPostProcessor(new SimpleDateTimePropertyMapper("born"))
                .mongoSelectorPostProcessor(new SimpleFieldNameMapper("@type", "atType"))
                .build()
                .compile(input);
    }
}