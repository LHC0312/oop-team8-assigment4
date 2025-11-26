package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team8.dto.BlockCreateRequest;
import team8.dto.BlockDto;
import team8.model.Block;
import team8.model.Project;
import team8.model.start.StartBlock;
import team8.model.control.*;
import team8.model.output.*;
import team8.model.arithmetic.*;
import team8.model.variable.*;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final BlockRepository blockRepository;
    private final ProjectRepository projectRepository;

    public List<BlockDto> getBlocksByProjectId(Long projectId) {
        return blockRepository.findByProjectIdOrderByOrderAsc(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BlockDto getBlockById(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Block not found"));
        return convertToDto(block);
    }

    @Transactional
    public BlockDto createBlock(Long projectId, BlockCreateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Block block = createBlockFromRequest(request, project);
        Block savedBlock = blockRepository.save(block);
        return convertToDto(savedBlock);
    }

    @Transactional
    public BlockDto updateBlock(Long id, BlockCreateRequest request) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Block not found"));

        updateBlockFromRequest(block, request);
        return convertToDto(block);
    }

    @Transactional
    public void deleteBlock(Long id) {
        blockRepository.deleteById(id);
    }

    private Block createBlockFromRequest(BlockCreateRequest request, Project project) {
        Block block;

        switch (request.getBlockType().toUpperCase()) {
            case "START":
                block = StartBlock.builder().build();
                break;
            case "IF":
                block = IfBlock.builder()
                        .conditionExpression(request.getConditionExpression())
                        .trueBranchId(request.getTrueBranchId())
                        .falseBranchId(request.getFalseBranchId())
                        .build();
                break;
            case "FOR":
                block = ForBlock.builder()
                        .conditionExpression(request.getConditionExpression())
                        .trueBranchId(request.getTrueBranchId())
                        .falseBranchId(request.getFalseBranchId())
                        .initExpression(request.getInitExpression())
                        .incrementExpression(request.getIncrementExpression())
                        .build();
                break;
            case "WHILE":
                block = WhileBlock.builder()
                        .conditionExpression(request.getConditionExpression())
                        .trueBranchId(request.getTrueBranchId())
                        .falseBranchId(request.getFalseBranchId())
                        .build();
                break;
            case "PRINT":
                block = PrintBlock.builder()
                        .message(request.getMessage())
                        .build();
                break;
            case "ALERT":
                block = AlertBlock.builder()
                        .message(request.getMessage())
                        .build();
                break;
            case "DRAW":
                block = DrawBlock.builder()
                        .shape(request.getShape())
                        .color(request.getColor())
                        .size(request.getSize())
                        .build();
                break;
            case "ADD":
                block = AddBlock.builder()
                        .operand1(request.getOperand1())
                        .operand2(request.getOperand2())
                        .resultVariable(request.getResultVariable())
                        .build();
                break;
            case "SUBTRACT":
                block = SubtractBlock.builder()
                        .operand1(request.getOperand1())
                        .operand2(request.getOperand2())
                        .resultVariable(request.getResultVariable())
                        .build();
                break;
            case "MULTIPLY":
                block = MultiplyBlock.builder()
                        .operand1(request.getOperand1())
                        .operand2(request.getOperand2())
                        .resultVariable(request.getResultVariable())
                        .build();
                break;
            case "DIVIDE":
                block = DivideBlock.builder()
                        .operand1(request.getOperand1())
                        .operand2(request.getOperand2())
                        .resultVariable(request.getResultVariable())
                        .build();
                break;
            case "VAR_DECLARE":
                block = VariableDeclareBlock.builder()
                        .variableName(request.getVariableName())
                        .variableType(request.getVariableType())
                        .initialValue(request.getInitialValue())
                        .build();
                break;
            case "VAR_ASSIGN":
                block = VariableAssignBlock.builder()
                        .variableName(request.getVariableName())
                        .valueExpression(request.getValueExpression())
                        .build();
                break;
            default:
                throw new RuntimeException("Unknown block type: " + request.getBlockType());
        }

        block.setPositionX(request.getPositionX());
        block.setPositionY(request.getPositionY());
        block.setOrder(request.getOrder());
        block.setNextBlockId(request.getNextBlockId());
        block.setProject(project);

        return block;
    }

    private void updateBlockFromRequest(Block block, BlockCreateRequest request) {
        block.setPositionX(request.getPositionX());
        block.setPositionY(request.getPositionY());
        block.setOrder(request.getOrder());
        block.setNextBlockId(request.getNextBlockId());

        if (block instanceof ControlBlock) {
            ControlBlock controlBlock = (ControlBlock) block;
            controlBlock.setConditionExpression(request.getConditionExpression());
            controlBlock.setTrueBranchId(request.getTrueBranchId());
            controlBlock.setFalseBranchId(request.getFalseBranchId());

            if (block instanceof ForBlock) {
                ForBlock forBlock = (ForBlock) block;
                forBlock.setInitExpression(request.getInitExpression());
                forBlock.setIncrementExpression(request.getIncrementExpression());
            }
        } else if (block instanceof PrintBlock) {
            ((PrintBlock) block).setMessage(request.getMessage());
        } else if (block instanceof AlertBlock) {
            ((AlertBlock) block).setMessage(request.getMessage());
        } else if (block instanceof DrawBlock) {
            DrawBlock drawBlock = (DrawBlock) block;
            drawBlock.setShape(request.getShape());
            drawBlock.setColor(request.getColor());
            drawBlock.setSize(request.getSize());
        } else if (block instanceof AddBlock) {
            AddBlock addBlock = (AddBlock) block;
            addBlock.setOperand1(request.getOperand1());
            addBlock.setOperand2(request.getOperand2());
            addBlock.setResultVariable(request.getResultVariable());
        } else if (block instanceof SubtractBlock) {
            SubtractBlock subtractBlock = (SubtractBlock) block;
            subtractBlock.setOperand1(request.getOperand1());
            subtractBlock.setOperand2(request.getOperand2());
            subtractBlock.setResultVariable(request.getResultVariable());
        } else if (block instanceof MultiplyBlock) {
            MultiplyBlock multiplyBlock = (MultiplyBlock) block;
            multiplyBlock.setOperand1(request.getOperand1());
            multiplyBlock.setOperand2(request.getOperand2());
            multiplyBlock.setResultVariable(request.getResultVariable());
        } else if (block instanceof DivideBlock) {
            DivideBlock divideBlock = (DivideBlock) block;
            divideBlock.setOperand1(request.getOperand1());
            divideBlock.setOperand2(request.getOperand2());
            divideBlock.setResultVariable(request.getResultVariable());
        } else if (block instanceof VariableDeclareBlock) {
            VariableDeclareBlock declareBlock = (VariableDeclareBlock) block;
            declareBlock.setVariableName(request.getVariableName());
            declareBlock.setVariableType(request.getVariableType());
            declareBlock.setInitialValue(request.getInitialValue());
        } else if (block instanceof VariableAssignBlock) {
            VariableAssignBlock assignBlock = (VariableAssignBlock) block;
            assignBlock.setVariableName(request.getVariableName());
            assignBlock.setValueExpression(request.getValueExpression());
        }
    }

    private BlockDto convertToDto(Block block) {
        BlockDto dto = BlockDto.builder()
                .id(block.getId())
                .blockType(block.getBlockType())
                .positionX(block.getPositionX())
                .positionY(block.getPositionY())
                .order(block.getOrder())
                .nextBlockId(block.getNextBlockId())
                .build();

        if (block instanceof ControlBlock) {
            ControlBlock controlBlock = (ControlBlock) block;
            dto.setConditionExpression(controlBlock.getConditionExpression());
            dto.setTrueBranchId(controlBlock.getTrueBranchId());
            dto.setFalseBranchId(controlBlock.getFalseBranchId());

            if (block instanceof ForBlock) {
                ForBlock forBlock = (ForBlock) block;
                dto.setInitExpression(forBlock.getInitExpression());
                dto.setIncrementExpression(forBlock.getIncrementExpression());
            }
        } else if (block instanceof PrintBlock) {
            dto.setMessage(((PrintBlock) block).getMessage());
        } else if (block instanceof AlertBlock) {
            dto.setMessage(((AlertBlock) block).getMessage());
        } else if (block instanceof DrawBlock) {
            DrawBlock drawBlock = (DrawBlock) block;
            dto.setShape(drawBlock.getShape());
            dto.setColor(drawBlock.getColor());
            dto.setSize(drawBlock.getSize());
        } else if (block instanceof AddBlock) {
            AddBlock addBlock = (AddBlock) block;
            dto.setOperand1(addBlock.getOperand1());
            dto.setOperand2(addBlock.getOperand2());
            dto.setResultVariable(addBlock.getResultVariable());
        } else if (block instanceof SubtractBlock) {
            SubtractBlock subtractBlock = (SubtractBlock) block;
            dto.setOperand1(subtractBlock.getOperand1());
            dto.setOperand2(subtractBlock.getOperand2());
            dto.setResultVariable(subtractBlock.getResultVariable());
        } else if (block instanceof MultiplyBlock) {
            MultiplyBlock multiplyBlock = (MultiplyBlock) block;
            dto.setOperand1(multiplyBlock.getOperand1());
            dto.setOperand2(multiplyBlock.getOperand2());
            dto.setResultVariable(multiplyBlock.getResultVariable());
        } else if (block instanceof DivideBlock) {
            DivideBlock divideBlock = (DivideBlock) block;
            dto.setOperand1(divideBlock.getOperand1());
            dto.setOperand2(divideBlock.getOperand2());
            dto.setResultVariable(divideBlock.getResultVariable());
        } else if (block instanceof VariableDeclareBlock) {
            VariableDeclareBlock declareBlock = (VariableDeclareBlock) block;
            dto.setVariableName(declareBlock.getVariableName());
            dto.setVariableType(declareBlock.getVariableType());
            dto.setInitialValue(declareBlock.getInitialValue());
        } else if (block instanceof VariableAssignBlock) {
            VariableAssignBlock assignBlock = (VariableAssignBlock) block;
            dto.setVariableName(assignBlock.getVariableName());
            dto.setValueExpression(assignBlock.getValueExpression());
        }

        return dto;
    }
}
