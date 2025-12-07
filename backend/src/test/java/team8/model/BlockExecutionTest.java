package team8.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;
import team8.model.control.IfBlock;
import team8.model.output.PrintBlock;
import team8.model.start.StartBlock;
import team8.model.variable.VariableDeclareBlock;

import static org.junit.jupiter.api.Assertions.*;

class BlockExecutionTest {

    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        context = new ExecutionContext(null, "test-session");
    }

    @Test
    @DisplayName("StartBlock 실행 테스트")
    void testStartBlockExecution() {
        StartBlock startBlock = StartBlock.builder()
                .id(1L)
                .nextBlockId(2L)
                .build();

        ExecutionResult result = startBlock.execute(context);

        assertEquals(2L, result.getNextBlockId());
        assertEquals("START", startBlock.getBlockType());
    }

    @Test
    @DisplayName("PrintBlock 실행 테스트")
    void testPrintBlockExecution() {
        PrintBlock printBlock = PrintBlock.builder()
                .id(1L)
                .messageExpressionBlock(team8.model.expression.LiteralExpressionBlock.builder()
                        .value("Hello World")
                        .literalType("STRING")
                        .build())
                .nextBlockId(2L)
                .build();

        ExecutionResult result = printBlock.execute(context);

        assertEquals(2L, result.getNextBlockId());
        assertEquals("PRINT", printBlock.getBlockType());
    }

    @Test
    @DisplayName("PrintBlock 변수 출력 테스트")
    void testPrintBlockWithVariable() {
        context.setVariable("x", 10);

        PrintBlock printBlock = PrintBlock.builder()
                .id(1L)
                .messageExpressionBlock(team8.model.expression.VariableExpressionBlock.builder()
                        .variableName("x")
                        .build())
                .nextBlockId(null)
                .build();

        ExecutionResult result = printBlock.execute(context);

        assertNull(result.getNextBlockId());
    }

    @Test
    @DisplayName("VariableDeclareBlock 실행 테스트")
    void testVariableDeclareBlockExecution() {
        VariableDeclareBlock declareBlock = VariableDeclareBlock.builder()
                .id(1L)
                .variableId(10L)
                .variableName("x")
                .variableType("number")
                .initialExpressionBlock(team8.model.expression.LiteralExpressionBlock.builder().value("10").literalType("NUMBER").build())
                .nextBlockId(2L)
                .build();

        ExecutionResult result = declareBlock.execute(context);

        assertEquals(2L, result.getNextBlockId());
        assertEquals(10.0, context.getVariable("x"));
    }

    @Test
    @DisplayName("VariableAssignBlock 표현식을 사용해 덧셈을 수행한다")
    void testVariableAssignAddition() {
        context.setVariable("a", 5);
        context.setVariable("b", 3);
        context.setVariableMeta(99L, "result", "number");

        team8.model.variable.VariableAssignBlock assignBlock = team8.model.variable.VariableAssignBlock.builder()
                .id(1L)
                .variableId(99L)
                .variableName("result")
                .valueExpressionBlock(team8.model.expression.BinaryExpressionBlock.builder()
                        .operator("+")
                        .left(team8.model.expression.VariableExpressionBlock.builder().variableName("a").build())
                        .right(team8.model.expression.VariableExpressionBlock.builder().variableName("b").build())
                        .build())
                .nextBlockId(2L)
                .build();

        ExecutionResult result = assignBlock.execute(context);

        assertEquals(2L, result.getNextBlockId());
        assertEquals(8.0, context.getVariable("result"));
    }

    @Test
    @DisplayName("VariableAssignBlock 리터럴 덧셈 테스트")
    void testVariableAssignWithLiterals() {
        context.setVariableMeta(100L, "sum", "number");

        team8.model.variable.VariableAssignBlock assignBlock = team8.model.variable.VariableAssignBlock.builder()
                .id(1L)
                .variableId(100L)
                .variableName("sum")
                .valueExpressionBlock(team8.model.expression.BinaryExpressionBlock.builder()
                        .operator("+")
                        .left(team8.model.expression.LiteralExpressionBlock.builder().value("10").literalType("NUMBER").build())
                        .right(team8.model.expression.LiteralExpressionBlock.builder().value("20").literalType("NUMBER").build())
                        .build())
                .nextBlockId(null)
                .build();

        ExecutionResult result = assignBlock.execute(context);

        assertNull(result.getNextBlockId());
        assertEquals(30.0, context.getVariable("sum"));
    }

    @Test
    @DisplayName("IfBlock 조건이 참일 때 테스트")
    void testIfBlockTrueBranch() {
        context.setVariable("x", 10);

        IfBlock ifBlock = IfBlock.builder()
                .id(1L)
                .conditionExpressionBlock(team8.model.expression.BinaryExpressionBlock.builder()
                        .operator(">")
                        .left(team8.model.expression.VariableExpressionBlock.builder().variableName("x").build())
                        .right(team8.model.expression.LiteralExpressionBlock.builder().value("5").literalType("NUMBER").build())
                        .build())
                .trueBranchId(2L)
                .falseBranchId(3L)
                .build();

        ExecutionResult result = ifBlock.execute(context);

        assertEquals(2L, result.getNextBlockId());
    }

    @Test
    @DisplayName("IfBlock 조건이 거짓일 때 테스트")
    void testIfBlockFalseBranch() {
        context.setVariable("x", 3);

        IfBlock ifBlock = IfBlock.builder()
                .id(1L)
                .conditionExpressionBlock(team8.model.expression.BinaryExpressionBlock.builder()
                        .operator(">")
                        .left(team8.model.expression.VariableExpressionBlock.builder().variableName("x").build())
                        .right(team8.model.expression.LiteralExpressionBlock.builder().value("5").literalType("NUMBER").build())
                        .build())
                .trueBranchId(2L)
                .falseBranchId(3L)
                .build();

        ExecutionResult result = ifBlock.execute(context);

        assertEquals(3L, result.getNextBlockId());
    }

    @Test
    @DisplayName("IfBlock false 분기가 없을 때 nextBlockId로 이동")
    void testIfBlockFalseBranchWithNextBlock() {
        context.setVariable("x", 3);

        IfBlock ifBlock = IfBlock.builder()
                .id(1L)
                .conditionExpressionBlock(team8.model.expression.BinaryExpressionBlock.builder()
                        .operator(">")
                        .left(team8.model.expression.VariableExpressionBlock.builder().variableName("x").build())
                        .right(team8.model.expression.LiteralExpressionBlock.builder().value("5").literalType("NUMBER").build())
                        .build())
                .trueBranchId(2L)
                .falseBranchId(null)
                .nextBlockId(4L)
                .build();

        ExecutionResult result = ifBlock.execute(context);

        assertEquals(4L, result.getNextBlockId());
    }
}
