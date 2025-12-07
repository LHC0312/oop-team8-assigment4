package team8.model.variable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team8.execution.ExecutionContext;
import team8.model.expression.LiteralExpressionBlock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableDeclareBlockTest {

    @Test
    @DisplayName("변수 선언 블록 실행 시 초기값으로 변수 생성")
    void executesAndCreatesVariableWithInitialValue() {
        ExecutionContext ctx = new ExecutionContext(null, "test");
        VariableDeclareBlock block = VariableDeclareBlock.builder()
                .variableId(1L)
                .variableName("i")
                .variableType("number")
                .initialExpressionBlock(LiteralExpressionBlock.builder().value("0").literalType("NUMBER").build())
                .build();

        block.execute(ctx);

        assertEquals(0.0, ctx.getVariable("i"));
        assertEquals("number", ctx.getVariableType(1L));
    }
}
