package team8.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;
import team8.model.arithmetic.AddBlock;
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
                .message("Hello World")
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
                .message("x")
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
                .variableName("x")
                .variableType("number")
                .initialValue("10")
                .nextBlockId(2L)
                .build();

        ExecutionResult result = declareBlock.execute(context);

        assertEquals(2L, result.getNextBlockId());
        assertEquals(10.0, context.getVariable("x"));
    }

    @Test
    @DisplayName("AddBlock 실행 테스트")
    void testAddBlockExecution() {
        context.setVariable("a", 5);
        context.setVariable("b", 3);

        AddBlock addBlock = AddBlock.builder()
                .id(1L)
                .operand1("a")
                .operand2("b")
                .resultVariable("result")
                .nextBlockId(2L)
                .build();

        ExecutionResult result = addBlock.execute(context);

        assertEquals(2L, result.getNextBlockId());
        assertEquals(8.0, context.getVariable("result"));
    }

    @Test
    @DisplayName("AddBlock 리터럴 값 테스트")
    void testAddBlockWithLiterals() {
        AddBlock addBlock = AddBlock.builder()
                .id(1L)
                .operand1("10")
                .operand2("20")
                .resultVariable("sum")
                .nextBlockId(null)
                .build();

        ExecutionResult result = addBlock.execute(context);

        assertNull(result.getNextBlockId());
        assertEquals(30.0, context.getVariable("sum"));
    }

    @Test
    @DisplayName("IfBlock 조건이 참일 때 테스트")
    void testIfBlockTrueBranch() {
        context.setVariable("x", 10);

        IfBlock ifBlock = IfBlock.builder()
                .id(1L)
                .conditionExpression("x > 5")
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
                .conditionExpression("x > 5")
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
                .conditionExpression("x > 5")
                .trueBranchId(2L)
                .falseBranchId(null)
                .nextBlockId(4L)
                .build();

        ExecutionResult result = ifBlock.execute(context);

        assertEquals(4L, result.getNextBlockId());
    }
}
