package team8.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import team8.dto.BlockCreateRequest;
import team8.dto.BlockDto;
import team8.dto.ProjectCreateRequest;
import team8.dto.ProjectDto;
import team8.dto.ValueDto;
import team8.service.BlockService;
import team8.service.ProjectService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProjectService projectService;
    private final BlockService blockService;

    @Override
    public void run(String... args) {
        log.info("Initializing test data...");

        if (!projectService.getAllProjects().isEmpty()) {
            log.info("Test data already exists. Skipping initialization.");
            return;
        }

        createHelloWorldProject();
        createConditionalProject();
        createLoopProject();
        createArithmeticProject();

        log.info("Test data initialization completed.");
    }

    private void createHelloWorldProject() {
        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Hello World", "Simple greeting program")
        );

        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null));

        BlockDto printBlock = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 200, 2, literal("Hello World!"), null));

        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, printBlock.getId()));
    }

    private void createConditionalProject() {
        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Conditional Test", "IF statement example")
        );

        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null));

        BlockDto varDeclare = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "x", "number", literal(10), null));
        ValueDto xVar = variable(varDeclare.getVariableId(), "x");

        ValueDto condition = binary(">", xVar, literal(5));
        BlockDto ifBlock = blockService.createBlock(project.getId(),
            createIfBlockRequest(100, 300, 3, condition, null, null));

        BlockDto trueBranch = blockService.createBlock(project.getId(),
            createPrintBlockRequest(200, 400, 4, literal("x is greater than 5"), null));
        BlockDto falseBranch = blockService.createBlock(project.getId(),
            createPrintBlockRequest(50, 400, 5, literal("x is not greater than 5"), null));

        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, varDeclare.getId()));
        blockService.updateBlock(varDeclare.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "x", "number", literal(10), ifBlock.getId()));
        blockService.updateBlock(ifBlock.getId(),
            createIfBlockRequest(100, 300, 3, condition, trueBranch.getId(), falseBranch.getId()));
    }

    private void createLoopProject() {
        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Loop Test", "WHILE loop example")
        );

        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null));

        BlockDto varDeclare = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "i", "number", literal(0), null));
        ValueDto iVar = variable(varDeclare.getVariableId(), "i");

        ValueDto whileCond = binary("<", iVar, literal(5));
        BlockDto whileBlock = blockService.createBlock(project.getId(),
            createWhileBlockRequest(100, 300, 3, whileCond, null, null));

        BlockDto printBlock = blockService.createBlock(project.getId(),
            createPrintBlockRequest(200, 400, 4, iVar, null));

        BlockDto addBlock = blockService.createBlock(project.getId(),
            createAddBlockRequest(200, 500, 5, iVar, literal(1), iVar.getVariableId(), "i", null));

        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, varDeclare.getId()));
        blockService.updateBlock(varDeclare.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "i", "number", literal(0), whileBlock.getId()));
        blockService.updateBlock(whileBlock.getId(),
            createWhileBlockRequest(100, 300, 3, whileCond, printBlock.getId(), null));
        blockService.updateBlock(printBlock.getId(),
            createPrintBlockRequest(200, 400, 4, iVar, addBlock.getId()));
        blockService.updateBlock(addBlock.getId(),
            createAddBlockRequest(200, 500, 5, iVar, literal(1), iVar.getVariableId(), "i", whileBlock.getId()));
    }

    private void createArithmeticProject() {
        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Arithmetic Operations", "Testing all arithmetic operations")
        );

        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null));

        BlockDto varA = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "a", "number", literal(10), null));
        BlockDto varB = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 300, 3, "b", "number", literal(5), null));

        BlockDto res1 = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 350, 4, "result1", "number", literal(0), null));
        BlockDto res2 = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 360, 5, "result2", "number", literal(0), null));
        BlockDto res3 = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 370, 6, "result3", "number", literal(0), null));
        BlockDto res4 = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 380, 7, "result4", "number", literal(0), null));

        ValueDto aVar = variable(varA.getVariableId(), "a");
        ValueDto bVar = variable(varB.getVariableId(), "b");

        BlockDto addBlock = blockService.createBlock(project.getId(),
            createAddBlockRequest(100, 400, 8, aVar, bVar, res1.getVariableId(), "result1", null));
        BlockDto print1 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 500, 9, literal("Addition: result1"), null));

        BlockDto subtractBlock = blockService.createBlock(project.getId(),
            createSubtractBlockRequest(100, 600, 10, aVar, bVar, res2.getVariableId(), "result2", null));
        BlockDto print2 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 700, 11, literal("Subtraction: result2"), null));

        BlockDto multiplyBlock = blockService.createBlock(project.getId(),
            createMultiplyBlockRequest(100, 800, 12, aVar, bVar, res3.getVariableId(), "result3", null));
        BlockDto print3 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 900, 13, literal("Multiplication: result3"), null));

        BlockDto divideBlock = blockService.createBlock(project.getId(),
            createDivideBlockRequest(100, 1000, 14, aVar, bVar, res4.getVariableId(), "result4", null));
        BlockDto print4 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 1100, 15, literal("Division: result4"), null));

        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, varA.getId()));
        blockService.updateBlock(varA.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "a", "number", literal(10), varB.getId()));
        blockService.updateBlock(varB.getId(),
            createVarDeclareBlockRequest(100, 300, 3, "b", "number", literal(5), res1.getId()));
        blockService.updateBlock(res1.getId(),
            createVarDeclareBlockRequest(100, 350, 4, "result1", "number", literal(0), res2.getId()));
        blockService.updateBlock(res2.getId(),
            createVarDeclareBlockRequest(100, 360, 5, "result2", "number", literal(0), res3.getId()));
        blockService.updateBlock(res3.getId(),
            createVarDeclareBlockRequest(100, 370, 6, "result3", "number", literal(0), res4.getId()));
        blockService.updateBlock(res4.getId(),
            createVarDeclareBlockRequest(100, 380, 7, "result4", "number", literal(0), addBlock.getId()));

        blockService.updateBlock(addBlock.getId(),
            createAddBlockRequest(100, 400, 8, aVar, bVar, res1.getVariableId(), "result1", print1.getId()));
        blockService.updateBlock(print1.getId(),
            createPrintBlockRequest(100, 500, 9, literal("Addition: result1"), subtractBlock.getId()));
        blockService.updateBlock(subtractBlock.getId(),
            createSubtractBlockRequest(100, 600, 10, aVar, bVar, res2.getVariableId(), "result2", print2.getId()));
        blockService.updateBlock(print2.getId(),
            createPrintBlockRequest(100, 700, 11, literal("Subtraction: result2"), multiplyBlock.getId()));
        blockService.updateBlock(multiplyBlock.getId(),
            createMultiplyBlockRequest(100, 800, 12, aVar, bVar, res3.getVariableId(), "result3", print3.getId()));
        blockService.updateBlock(print3.getId(),
            createPrintBlockRequest(100, 900, 13, literal("Multiplication: result3"), divideBlock.getId()));
        blockService.updateBlock(divideBlock.getId(),
            createDivideBlockRequest(100, 1000, 14, aVar, bVar, res4.getVariableId(), "result4", print4.getId()));
    }

    // Helper methods
    private BlockCreateRequest createBlockRequest(String blockType, int x, int y, int order, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType(blockType);
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setNextBlockId(nextBlockId);
        return request;
    }

    private BlockCreateRequest createPrintBlockRequest(int x, int y, int order, ValueDto message, Long nextBlockId) {
        BlockCreateRequest request = createBlockRequest("PRINT", x, y, order, nextBlockId);
        request.setMessage(message);
        return request;
    }

    private BlockCreateRequest createVarDeclareBlockRequest(int x, int y, int order, String varName, String type, ValueDto initial, Long nextBlockId) {
        BlockCreateRequest request = createBlockRequest("VAR_DECLARE", x, y, order, nextBlockId);
        request.setVariableName(varName);
        request.setVariableType(type);
        request.setInitial(initial);
        return request;
    }

    private BlockCreateRequest createIfBlockRequest(int x, int y, int order, ValueDto condition, Long trueBranchId, Long falseBranchId) {
        BlockCreateRequest request = createBlockRequest("IF", x, y, order, null);
        request.setCondition(condition);
        request.setTrueBranchId(trueBranchId);
        request.setFalseBranchId(falseBranchId);
        return request;
    }

    private BlockCreateRequest createWhileBlockRequest(int x, int y, int order, ValueDto condition, Long trueBranchId, Long nextBlockId) {
        BlockCreateRequest request = createBlockRequest("WHILE", x, y, order, nextBlockId);
        request.setCondition(condition);
        request.setTrueBranchId(trueBranchId);
        return request;
    }

    private BlockCreateRequest createAddBlockRequest(int x, int y, int order,
                                                    ValueDto operand1, ValueDto operand2,
                                                    Long resultVarId, String resultVarName, Long nextBlockId) {
        BlockCreateRequest request = createBlockRequest("ADD", x, y, order, nextBlockId);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariableId(resultVarId);
        request.setResultVariable(resultVarName);
        return request;
    }

    private BlockCreateRequest createSubtractBlockRequest(int x, int y, int order,
                                                          ValueDto operand1, ValueDto operand2,
                                                          Long resultVarId, String resultVarName, Long nextBlockId) {
        BlockCreateRequest request = createBlockRequest("SUBTRACT", x, y, order, nextBlockId);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariableId(resultVarId);
        request.setResultVariable(resultVarName);
        return request;
    }

    private BlockCreateRequest createMultiplyBlockRequest(int x, int y, int order,
                                                          ValueDto operand1, ValueDto operand2,
                                                          Long resultVarId, String resultVarName, Long nextBlockId) {
        BlockCreateRequest request = createBlockRequest("MULTIPLY", x, y, order, nextBlockId);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariableId(resultVarId);
        request.setResultVariable(resultVarName);
        return request;
    }

    private BlockCreateRequest createDivideBlockRequest(int x, int y, int order,
                                                        ValueDto operand1, ValueDto operand2,
                                                        Long resultVarId, String resultVarName, Long nextBlockId) {
        BlockCreateRequest request = createBlockRequest("DIVIDE", x, y, order, nextBlockId);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariableId(resultVarId);
        request.setResultVariable(resultVarName);
        return request;
    }

    private ValueDto literal(Object value) {
        return ValueDto.builder()
                .valueType("LITERAL")
                .data(value)
                .build();
    }

    private ValueDto variable(Long id, String name) {
        return ValueDto.builder()
                .valueType("VARIABLE")
                .variableId(id)
                .variableName(name)
                .build();
    }

    private ValueDto binary(String operator, ValueDto left, ValueDto right) {
        return ValueDto.builder()
                .valueType("BINARY")
                .operator(operator)
                .left(left)
                .right(right)
                .build();
    }
}
