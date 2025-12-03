package team8.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team8.model.expression.BinaryExpressionBlock;
import team8.model.expression.LiteralExpressionBlock;
import team8.model.expression.UnaryExpressionBlock;
import team8.model.expression.VariableExpressionBlock;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionContextTest {

    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        context = new ExecutionContext(null, "test-session");
    }

    @Test
    @DisplayName("변수 저장 및 조회 테스트")
    void testVariableStorageAndRetrieval() {
        context.setVariable("x", 10);
        context.setVariable("name", "test");

        assertEquals(10, context.getVariable("x"));
        assertEquals("test", context.getVariable("name"));
    }

    @Test
    @DisplayName("숫자 표현식 평가 테스트")
    void testEvaluateNumericExpression() {
        LiteralExpressionBlock lit = LiteralExpressionBlock.builder()
                .value("42")
                .literalType("NUMBER")
                .build();
        Object result = context.evaluateExpression(lit);
        assertEquals(42.0, result);
    }

    @Test
    @DisplayName("문자열 표현식 평가 테스트")
    void testEvaluateStringExpression() {
        LiteralExpressionBlock lit = LiteralExpressionBlock.builder()
                .value("Hello World")
                .literalType("STRING")
                .build();
        Object result = context.evaluateExpression(lit);
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("변수 참조 표현식 평가 테스트")
    void testEvaluateVariableReference() {
        context.setVariable("x", 100);
        VariableExpressionBlock var = VariableExpressionBlock.builder()
                .variableName("x")
                .build();
        Object result = context.evaluateExpression(var);
        assertEquals(100, result);
    }

    @Test
    @DisplayName("== 조건 평가 테스트")
    void testEvaluateEqualityCondition() {
        context.setVariable("x", 5);

        BinaryExpressionBlock eq = BinaryExpressionBlock.builder()
                .operator("==")
                .left(VariableExpressionBlock.builder().variableName("x").build())
                .right(LiteralExpressionBlock.builder().value("5").literalType("NUMBER").build())
                .build();

        assertTrue(context.evaluateCondition(eq));

        BinaryExpressionBlock neq = BinaryExpressionBlock.builder()
                .operator("==")
                .left(VariableExpressionBlock.builder().variableName("x").build())
                .right(LiteralExpressionBlock.builder().value("10").literalType("NUMBER").build())
                .build();
        assertFalse(context.evaluateCondition(neq));
    }

    @Test
    @DisplayName("> 조건 평가 테스트")
    void testEvaluateGreaterThanCondition() {
        context.setVariable("x", 10);

        BinaryExpressionBlock gt = BinaryExpressionBlock.builder()
                .operator(">")
                .left(VariableExpressionBlock.builder().variableName("x").build())
                .right(LiteralExpressionBlock.builder().value("5").literalType("NUMBER").build())
                .build();
        assertTrue(context.evaluateCondition(gt));

        BinaryExpressionBlock gtFalse = BinaryExpressionBlock.builder()
                .operator(">")
                .left(VariableExpressionBlock.builder().variableName("x").build())
                .right(LiteralExpressionBlock.builder().value("15").literalType("NUMBER").build())
                .build();
        assertFalse(context.evaluateCondition(gtFalse));
    }

    @Test
    @DisplayName("< 조건 평가 테스트")
    void testEvaluateLessThanCondition() {
        context.setVariable("x", 10);

        BinaryExpressionBlock lt = BinaryExpressionBlock.builder()
                .operator("<")
                .left(VariableExpressionBlock.builder().variableName("x").build())
                .right(LiteralExpressionBlock.builder().value("15").literalType("NUMBER").build())
                .build();
        assertTrue(context.evaluateCondition(lt));

        BinaryExpressionBlock ltFalse = BinaryExpressionBlock.builder()
                .operator("<")
                .left(VariableExpressionBlock.builder().variableName("x").build())
                .right(LiteralExpressionBlock.builder().value("5").literalType("NUMBER").build())
                .build();
        assertFalse(context.evaluateCondition(ltFalse));
    }

    @Test
    @DisplayName("정지 상태 테스트")
    void testStopFunctionality() {
        assertFalse(context.isStopped());

        context.stop();

        assertTrue(context.isStopped());
    }

    @Test
    @DisplayName("문자열 + 문자열은 연결된다")
    void testStringConcatenation() {
        BinaryExpressionBlock expr = BinaryExpressionBlock.builder()
                .operator("+")
                .left(LiteralExpressionBlock.builder().value("a").literalType("STRING").build())
                .right(LiteralExpressionBlock.builder().value("b").literalType("STRING").build())
                .build();
        Object result = context.evaluateExpression(expr);
        assertEquals("ab", result);
    }

    @Test
    @DisplayName("문자열 + 숫자는 예외를 던진다")
    void testStringPlusNumberThrows() {
        BinaryExpressionBlock expr = BinaryExpressionBlock.builder()
                .operator("+")
                .left(LiteralExpressionBlock.builder().value("a").literalType("STRING").build())
                .right(LiteralExpressionBlock.builder().value("1").literalType("NUMBER").build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> context.evaluateExpression(expr));
    }
}
