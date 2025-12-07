package team8.model.variable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team8.execution.ExecutionContext;
import team8.model.expression.BinaryExpressionBlock;
import team8.model.expression.LiteralExpressionBlock;
import team8.model.expression.VariableExpressionBlock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableAssignBlockTest {

    @Test
    @DisplayName("값 표현식으로 변수를 증가시킨다")
    void executesAndUpdatesVariable() {
        ExecutionContext ctx = new ExecutionContext(null, "test");
        ctx.setVariableType("i", "number");
        ctx.setVariable("i", 0);

        VariableAssignBlock block = VariableAssignBlock.builder()
                .variableName("i")
                .variableId(1L)
                .valueExpressionBlock(
                        BinaryExpressionBlock.builder()
                                .operator("+")
                                .left(VariableExpressionBlock.builder().variableName("i").build())
                                .right(LiteralExpressionBlock.builder().value("1").literalType("NUMBER").build())
                                .build()
                )
                .build();

        block.execute(ctx);

        assertEquals(1.0, ctx.getVariable("i"));
    }
}
