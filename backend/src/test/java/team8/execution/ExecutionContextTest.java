package team8.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

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
        Object result = context.evaluateExpression("42");
        assertEquals(42.0, result);
    }

    @Test
    @DisplayName("문자열 표현식 평가 테스트")
    void testEvaluateStringExpression() {
        Object result = context.evaluateExpression("\"Hello World\"");
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("변수 참조 표현식 평가 테스트")
    void testEvaluateVariableReference() {
        context.setVariable("x", 100);
        Object result = context.evaluateExpression("x");
        assertEquals(100, result);
    }

    @Test
    @DisplayName("== 조건 평가 테스트")
    void testEvaluateEqualityCondition() {
        context.setVariable("x", 5);

        assertTrue(context.evaluateCondition("x == 5"));
        assertFalse(context.evaluateCondition("x == 10"));
    }

    @Test
    @DisplayName("> 조건 평가 테스트")
    void testEvaluateGreaterThanCondition() {
        context.setVariable("x", 10);

        assertTrue(context.evaluateCondition("x > 5"));
        assertFalse(context.evaluateCondition("x > 15"));
    }

    @Test
    @DisplayName("< 조건 평가 테스트")
    void testEvaluateLessThanCondition() {
        context.setVariable("x", 10);

        assertTrue(context.evaluateCondition("x < 15"));
        assertFalse(context.evaluateCondition("x < 5"));
    }

    @Test
    @DisplayName("정지 상태 테스트")
    void testStopFunctionality() {
        assertFalse(context.isStopped());

        context.stop();

        assertTrue(context.isStopped());
    }

    @Test
    @DisplayName("null 또는 빈 표현식 평가 테스트")
    void testEvaluateNullOrEmptyExpression() {
        assertNull(context.evaluateExpression(null));
        assertNull(context.evaluateExpression(""));
        assertNull(context.evaluateExpression("   "));
    }

    @Test
    @DisplayName("null 조건 평가 테스트")
    void testEvaluateNullCondition() {
        assertFalse(context.evaluateCondition(null));
        assertFalse(context.evaluateCondition(""));
    }
}
