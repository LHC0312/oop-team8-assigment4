package team8.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import team8.dto.BlockCreateRequest;
import team8.dto.BlockDto;
import team8.dto.ProjectCreateRequest;
import team8.dto.ProjectDto;
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

        // Check if data already exists
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
        log.info("Creating Hello World project...");

        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Hello World", "Simple greeting program")
        );

        // 1. START block
        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null, null));

        // 2. PRINT block
        BlockDto printBlock = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 200, 2, "Hello World!", null));

        // Update START block to point to PRINT block
        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, printBlock.getId(), null));

        log.info("Hello World project created with ID: {}", project.getId());
    }

    private void createConditionalProject() {
        log.info("Creating Conditional project...");

        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Conditional Test", "IF statement example")
        );

        // 1. START block
        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null, null));

        // 2. Variable declaration: x = 10
        BlockDto varDeclare = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "x", "10", null));

        // 3. IF block: x > 5
        BlockDto ifBlock = blockService.createBlock(project.getId(),
            createIfBlockRequest(100, 300, 3, "x > 5", null, null));

        // 4. True branch: PRINT "x is greater than 5"
        BlockDto trueBranch = blockService.createBlock(project.getId(),
            createPrintBlockRequest(200, 400, 4, "x is greater than 5", null));

        // 5. False branch: PRINT "x is not greater than 5"
        BlockDto falseBranch = blockService.createBlock(project.getId(),
            createPrintBlockRequest(50, 400, 5, "x is not greater than 5", null));

        // Update connections
        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, varDeclare.getId(), null));

        blockService.updateBlock(varDeclare.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "x", "10", ifBlock.getId()));

        blockService.updateBlock(ifBlock.getId(),
            createIfBlockRequest(100, 300, 3, "x > 5", trueBranch.getId(), falseBranch.getId()));

        log.info("Conditional project created with ID: {}", project.getId());
    }

    private void createLoopProject() {
        log.info("Creating Loop project...");

        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Loop Test", "WHILE loop example")
        );

        // 1. START block
        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null, null));

        // 2. Variable declaration: i = 0
        BlockDto varDeclare = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "i", "0", null));

        // 3. WHILE block: i < 5
        BlockDto whileBlock = blockService.createBlock(project.getId(),
            createWhileBlockRequest(100, 300, 3, "i < 5", null, null));

        // 4. PRINT block: print i
        BlockDto printBlock = blockService.createBlock(project.getId(),
            createPrintBlockRequest(200, 400, 4, "i", null));

        // 5. ADD block: i = i + 1
        BlockDto addBlock = blockService.createBlock(project.getId(),
            createAddBlockRequest(200, 500, 5, "i", "1", "i", null));

        // Update connections
        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, varDeclare.getId(), null));

        blockService.updateBlock(varDeclare.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "i", "0", whileBlock.getId()));

        blockService.updateBlock(whileBlock.getId(),
            createWhileBlockRequest(100, 300, 3, "i < 5", printBlock.getId(), null));

        blockService.updateBlock(printBlock.getId(),
            createPrintBlockRequest(200, 400, 4, "i", addBlock.getId()));

        blockService.updateBlock(addBlock.getId(),
            createAddBlockRequest(200, 500, 5, "i", "1", "i", whileBlock.getId()));

        log.info("Loop project created with ID: {}", project.getId());
    }

    private void createArithmeticProject() {
        log.info("Creating Arithmetic project...");

        ProjectDto project = projectService.createProject(
            new ProjectCreateRequest("Arithmetic Operations", "Testing all arithmetic operations")
        );

        // 1. START block
        BlockDto startBlock = blockService.createBlock(project.getId(),
            createBlockRequest("START", 100, 100, 1, null, null));

        // 2. Variable declaration: a = 10
        BlockDto varA = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "a", "10", null));

        // 3. Variable declaration: b = 5
        BlockDto varB = blockService.createBlock(project.getId(),
            createVarDeclareBlockRequest(100, 300, 3, "b", "5", null));

        // 4. ADD: result1 = a + b
        BlockDto addBlock = blockService.createBlock(project.getId(),
            createAddBlockRequest(100, 400, 4, "a", "b", "result1", null));

        // 5. PRINT result1
        BlockDto print1 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 500, 5, "Addition: result1", null));

        // 6. SUBTRACT: result2 = a - b
        BlockDto subtractBlock = blockService.createBlock(project.getId(),
            createSubtractBlockRequest(100, 600, 6, "a", "b", "result2", null));

        // 7. PRINT result2
        BlockDto print2 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 700, 7, "Subtraction: result2", null));

        // 8. MULTIPLY: result3 = a * b
        BlockDto multiplyBlock = blockService.createBlock(project.getId(),
            createMultiplyBlockRequest(100, 800, 8, "a", "b", "result3", null));

        // 9. PRINT result3
        BlockDto print3 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 900, 9, "Multiplication: result3", null));

        // 10. DIVIDE: result4 = a / b
        BlockDto divideBlock = blockService.createBlock(project.getId(),
            createDivideBlockRequest(100, 1000, 10, "a", "b", "result4", null));

        // 11. PRINT result4
        BlockDto print4 = blockService.createBlock(project.getId(),
            createPrintBlockRequest(100, 1100, 11, "Division: result4", null));

        // Update connections
        blockService.updateBlock(startBlock.getId(),
            createBlockRequest("START", 100, 100, 1, varA.getId(), null));

        blockService.updateBlock(varA.getId(),
            createVarDeclareBlockRequest(100, 200, 2, "a", "10", varB.getId()));

        blockService.updateBlock(varB.getId(),
            createVarDeclareBlockRequest(100, 300, 3, "b", "5", addBlock.getId()));

        blockService.updateBlock(addBlock.getId(),
            createAddBlockRequest(100, 400, 4, "a", "b", "result1", print1.getId()));

        blockService.updateBlock(print1.getId(),
            createPrintBlockRequest(100, 500, 5, "Addition: result1", subtractBlock.getId()));

        blockService.updateBlock(subtractBlock.getId(),
            createSubtractBlockRequest(100, 600, 6, "a", "b", "result2", print2.getId()));

        blockService.updateBlock(print2.getId(),
            createPrintBlockRequest(100, 700, 7, "Subtraction: result2", multiplyBlock.getId()));

        blockService.updateBlock(multiplyBlock.getId(),
            createMultiplyBlockRequest(100, 800, 8, "a", "b", "result3", print3.getId()));

        blockService.updateBlock(print3.getId(),
            createPrintBlockRequest(100, 900, 9, "Multiplication: result3", divideBlock.getId()));

        blockService.updateBlock(divideBlock.getId(),
            createDivideBlockRequest(100, 1000, 10, "a", "b", "result4", print4.getId()));

        log.info("Arithmetic project created with ID: {}", project.getId());
    }

    // Helper methods for creating block requests
    private BlockCreateRequest createBlockRequest(String blockType, int x, int y, int order, Long nextBlockId, String message) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType(blockType);
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setNextBlockId(nextBlockId);
        request.setMessage(message);
        return request;
    }

    private BlockCreateRequest createPrintBlockRequest(int x, int y, int order, String message, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("PRINT");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setMessage(message);
        request.setNextBlockId(nextBlockId);
        return request;
    }

    private BlockCreateRequest createVarDeclareBlockRequest(int x, int y, int order, String varName, String initialValue, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("VAR_DECLARE");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setVariableName(varName);
        request.setInitialValue(initialValue);
        request.setNextBlockId(nextBlockId);
        return request;
    }

    private BlockCreateRequest createIfBlockRequest(int x, int y, int order, String condition, Long trueBranchId, Long falseBranchId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("IF");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setConditionExpression(condition);
        request.setTrueBranchId(trueBranchId);
        request.setFalseBranchId(falseBranchId);
        return request;
    }

    private BlockCreateRequest createWhileBlockRequest(int x, int y, int order, String condition, Long trueBranchId, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("WHILE");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setConditionExpression(condition);
        request.setTrueBranchId(trueBranchId);
        request.setNextBlockId(nextBlockId);
        return request;
    }

    private BlockCreateRequest createAddBlockRequest(int x, int y, int order, String operand1, String operand2, String resultVar, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("ADD");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariable(resultVar);
        request.setNextBlockId(nextBlockId);
        return request;
    }

    private BlockCreateRequest createSubtractBlockRequest(int x, int y, int order, String operand1, String operand2, String resultVar, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("SUBTRACT");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariable(resultVar);
        request.setNextBlockId(nextBlockId);
        return request;
    }

    private BlockCreateRequest createMultiplyBlockRequest(int x, int y, int order, String operand1, String operand2, String resultVar, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("MULTIPLY");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariable(resultVar);
        request.setNextBlockId(nextBlockId);
        return request;
    }

    private BlockCreateRequest createDivideBlockRequest(int x, int y, int order, String operand1, String operand2, String resultVar, Long nextBlockId) {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("DIVIDE");
        request.setPositionX(x);
        request.setPositionY(y);
        request.setOrder(order);
        request.setOperand1(operand1);
        request.setOperand2(operand2);
        request.setResultVariable(resultVar);
        request.setNextBlockId(nextBlockId);
        return request;
    }
}
