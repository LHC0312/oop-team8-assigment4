package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team8.dto.BlockCreateRequest;
import team8.dto.BlockDto;
import team8.dto.ValueDto;
import team8.model.Block;
import team8.model.Project;
import team8.model.arithmetic.AddBlock;
import team8.model.arithmetic.DivideBlock;
import team8.model.arithmetic.MultiplyBlock;
import team8.model.arithmetic.SubtractBlock;
import team8.model.control.ControlBlock;
import team8.model.control.ForBlock;
import team8.model.control.IfBlock;
import team8.model.control.WhileBlock;
import team8.model.expression.BinaryExpressionBlock;
import team8.model.expression.ExpressionBlock;
import team8.model.expression.LiteralExpressionBlock;
import team8.model.expression.UnaryExpressionBlock;
import team8.model.expression.VariableExpressionBlock;
import team8.model.output.PrintBlock;
import team8.model.start.StartBlock;
import team8.model.variable.VariableAssignBlock;
import team8.model.variable.VariableDeclareBlock;
import team8.model.variable.Variable;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;
import team8.repository.ExpressionRepository;
import team8.repository.VariableRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final BlockRepository blockRepository;
    private final ProjectRepository projectRepository;
    private final ExpressionRepository expressionRepository;
    private final VariableRepository variableRepository;
    public List<BlockDto> getBlocksByProjectId(Long projectId) {
        return blockRepository.findByProjectIdOrderByOrderAsc(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BlockDto getBlockById(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Block not found: " + id));
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
                        .conditionExpressionBlock(buildExpression(request.getCondition(), project))
                        .trueBranchId(request.getTrueBranchId())
                        .falseBranchId(request.getFalseBranchId())
                        .build();
                break;
            case "FOR":
                block = ForBlock.builder()
                        .conditionExpressionBlock(buildExpression(request.getCondition(), project))
                        .trueBranchId(request.getTrueBranchId())
                        .falseBranchId(request.getFalseBranchId())
                        .initExpressionBlock(buildExpression(request.getInit(), project))
                        .incrementExpressionBlock(buildExpression(request.getIncrement(), project))
                        .build();
                break;
            case "WHILE":
                block = WhileBlock.builder()
                        .conditionExpressionBlock(buildExpression(request.getCondition(), project))
                        .trueBranchId(request.getTrueBranchId())
                        .falseBranchId(request.getFalseBranchId())
                        .build();
                break;
            case "PRINT":
                block = PrintBlock.builder()
                        .messageExpressionBlock(buildExpression(request.getMessage(), project))
                        .build();
                break;
            case "ADD":
                block = AddBlock.builder()
                        .operand1Block(buildExpression(request.getOperand1(), project))
                        .operand2Block(buildExpression(request.getOperand2(), project))
                        .resultVariable(request.getResultVariable())
                        .resultVariableId(request.getResultVariableId())
                        .build();
                break;
            case "SUBTRACT":
                block = SubtractBlock.builder()
                        .operand1Block(buildExpression(request.getOperand1(), project))
                        .operand2Block(buildExpression(request.getOperand2(), project))
                        .resultVariable(request.getResultVariable())
                        .resultVariableId(request.getResultVariableId())
                        .build();
                break;
            case "MULTIPLY":
                block = MultiplyBlock.builder()
                        .operand1Block(buildExpression(request.getOperand1(), project))
                        .operand2Block(buildExpression(request.getOperand2(), project))
                        .resultVariable(request.getResultVariable())
                        .resultVariableId(request.getResultVariableId())
                        .build();
                break;
            case "DIVIDE":
                block = DivideBlock.builder()
                        .operand1Block(buildExpression(request.getOperand1(), project))
                        .operand2Block(buildExpression(request.getOperand2(), project))
                        .resultVariable(request.getResultVariable())
                        .resultVariableId(request.getResultVariableId())
                        .build();
                break;
            case "VAR_DECLARE":
                Variable variable = createOrGetVariable(project, request.getVariableName(), request.getVariableType());
                block = VariableDeclareBlock.builder()
                        .variableName(variable.getName())
                        .variableId(variable.getId())
                        .variableType(variable.getType())
                        .initialExpressionBlock(buildExpression(request.getInitial(), project))
                        .build();
                break;
            case "VAR_ASSIGN":
                Variable target = resolveVariable(project, request.getVariableId(), request.getVariableName());
                block = VariableAssignBlock.builder()
                        .variableName(target.getName())
                        .variableId(target.getId())
                        .valueExpressionBlock(buildExpression(request.getValue(), project))
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
        setIfNotNull(block::setPositionX, request.getPositionX());
        setIfNotNull(block::setPositionY, request.getPositionY());
        setIfNotNull(block::setOrder, request.getOrder());
        if (request.getNextBlockId() != null) {
            block.setNextBlockId(request.getNextBlockId());
        }

        if (block instanceof ControlBlock controlBlock) {
            if (request.getCondition() != null) {
                controlBlock.setConditionExpressionBlock(buildExpression(request.getCondition(), controlBlock.getProject()));
            }
            if (request.getTrueBranchId() != null) {
                controlBlock.setTrueBranchId(request.getTrueBranchId());
            }
            if (request.getFalseBranchId() != null) {
                controlBlock.setFalseBranchId(request.getFalseBranchId());
            }

            if (block instanceof ForBlock forBlock) {
                if (request.getInit() != null) {
                    forBlock.setInitExpressionBlock(buildExpression(request.getInit(), controlBlock.getProject()));
                }
                if (request.getIncrement() != null) {
                    forBlock.setIncrementExpressionBlock(buildExpression(request.getIncrement(), controlBlock.getProject()));
                }
            }
        } else if (block instanceof PrintBlock printBlock) {
            if (request.getMessage() != null) {
                printBlock.setMessageExpressionBlock(buildExpression(request.getMessage(), printBlock.getProject()));
            }
        } else if (block instanceof AddBlock addBlock) {
            if (request.getOperand1() != null) {
                addBlock.setOperand1Block(buildExpression(request.getOperand1(), addBlock.getProject()));
            }
            if (request.getOperand2() != null) {
                addBlock.setOperand2Block(buildExpression(request.getOperand2(), addBlock.getProject()));
            }
            if (request.getResultVariable() != null) {
                addBlock.setResultVariable(request.getResultVariable());
            }
            if (request.getResultVariableId() != null) {
                addBlock.setResultVariableId(request.getResultVariableId());
            }
        } else if (block instanceof SubtractBlock subtractBlock) {
            if (request.getOperand1() != null) {
                subtractBlock.setOperand1Block(buildExpression(request.getOperand1(), subtractBlock.getProject()));
            }
            if (request.getOperand2() != null) {
                subtractBlock.setOperand2Block(buildExpression(request.getOperand2(), subtractBlock.getProject()));
            }
            if (request.getResultVariable() != null) {
                subtractBlock.setResultVariable(request.getResultVariable());
            }
            if (request.getResultVariableId() != null) {
                subtractBlock.setResultVariableId(request.getResultVariableId());
            }
        } else if (block instanceof MultiplyBlock multiplyBlock) {
            if (request.getOperand1() != null) {
                multiplyBlock.setOperand1Block(buildExpression(request.getOperand1(), multiplyBlock.getProject()));
            }
            if (request.getOperand2() != null) {
                multiplyBlock.setOperand2Block(buildExpression(request.getOperand2(), multiplyBlock.getProject()));
            }
            if (request.getResultVariable() != null) {
                multiplyBlock.setResultVariable(request.getResultVariable());
            }
            if (request.getResultVariableId() != null) {
                multiplyBlock.setResultVariableId(request.getResultVariableId());
            }
        } else if (block instanceof DivideBlock divideBlock) {
            if (request.getOperand1() != null) {
                divideBlock.setOperand1Block(buildExpression(request.getOperand1(), divideBlock.getProject()));
            }
            if (request.getOperand2() != null) {
                divideBlock.setOperand2Block(buildExpression(request.getOperand2(), divideBlock.getProject()));
            }
            if (request.getResultVariable() != null) {
                divideBlock.setResultVariable(request.getResultVariable());
            }
            if (request.getResultVariableId() != null) {
                divideBlock.setResultVariableId(request.getResultVariableId());
            }
        } else if (block instanceof VariableDeclareBlock declareBlock) {
            if (request.getVariableName() != null) {
                declareBlock.setVariableName(request.getVariableName());
            }
            if (request.getVariableType() != null) {
                declareBlock.setVariableType(request.getVariableType());
            }
            if (request.getInitial() != null) {
                declareBlock.setInitialExpressionBlock(buildExpression(request.getInitial(), declareBlock.getProject()));
            }
            if (request.getVariableId() != null) {
                declareBlock.setVariableId(request.getVariableId());
            }
        } else if (block instanceof VariableAssignBlock assignBlock) {
            if (request.getVariableName() != null) {
                assignBlock.setVariableName(request.getVariableName());
            }
            if (request.getVariableId() != null) {
                assignBlock.setVariableId(request.getVariableId());
            }
            if (request.getValue() != null) {
                assignBlock.setValueExpressionBlock(buildExpression(request.getValue(), assignBlock.getProject()));
            }
        }
    }

    private <T> void setIfNotNull(java.util.function.Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
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
            dto.setCondition(BlockServiceHelper.toValueDto(controlBlock.getConditionExpressionBlock()));
            dto.setTrueBranchId(controlBlock.getTrueBranchId());
            dto.setFalseBranchId(controlBlock.getFalseBranchId());

            if (block instanceof ForBlock forBlock) {
                dto.setInit(BlockServiceHelper.toValueDto(forBlock.getInitExpressionBlock()));
                dto.setIncrement(BlockServiceHelper.toValueDto(forBlock.getIncrementExpressionBlock()));
            }
        } else if (block instanceof PrintBlock printBlock) {
            dto.setMessage(BlockServiceHelper.toValueDto(printBlock.getMessageExpressionBlock()));
        } else if (block instanceof AddBlock addBlock) {
            dto.setOperand1(BlockServiceHelper.toValueDto(addBlock.getOperand1Block()));
            dto.setOperand2(BlockServiceHelper.toValueDto(addBlock.getOperand2Block()));
            dto.setResultVariable(addBlock.getResultVariable());
            dto.setResultVariableId(addBlock.getResultVariableId());
        } else if (block instanceof SubtractBlock subtractBlock) {
            dto.setOperand1(BlockServiceHelper.toValueDto(subtractBlock.getOperand1Block()));
            dto.setOperand2(BlockServiceHelper.toValueDto(subtractBlock.getOperand2Block()));
            dto.setResultVariable(subtractBlock.getResultVariable());
            dto.setResultVariableId(subtractBlock.getResultVariableId());
        } else if (block instanceof MultiplyBlock multiplyBlock) {
            dto.setOperand1(BlockServiceHelper.toValueDto(multiplyBlock.getOperand1Block()));
            dto.setOperand2(BlockServiceHelper.toValueDto(multiplyBlock.getOperand2Block()));
            dto.setResultVariable(multiplyBlock.getResultVariable());
            dto.setResultVariableId(multiplyBlock.getResultVariableId());
        } else if (block instanceof DivideBlock divideBlock) {
            dto.setOperand1(BlockServiceHelper.toValueDto(divideBlock.getOperand1Block()));
            dto.setOperand2(BlockServiceHelper.toValueDto(divideBlock.getOperand2Block()));
            dto.setResultVariable(divideBlock.getResultVariable());
            dto.setResultVariableId(divideBlock.getResultVariableId());
        } else if (block instanceof VariableDeclareBlock declareBlock) {
            dto.setVariableName(declareBlock.getVariableName());
            dto.setVariableType(declareBlock.getVariableType());
            dto.setVariableId(declareBlock.getVariableId());
            dto.setInitial(BlockServiceHelper.toValueDto(declareBlock.getInitialExpressionBlock()));
        } else if (block instanceof VariableAssignBlock assignBlock) {
            dto.setVariableName(assignBlock.getVariableName());
            dto.setVariableId(assignBlock.getVariableId());
            dto.setValue(BlockServiceHelper.toValueDto(assignBlock.getValueExpressionBlock()));
        }

        return dto;
    }

    private ExpressionBlock buildExpression(ValueDto dto, Project project) {
        if (dto == null) {
            return null;
        }

        if (dto.getBlockId() != null) {
            ExpressionBlock existing = expressionRepository.findById(dto.getBlockId())
                    .orElseThrow(() -> new IllegalArgumentException("Expression not found: " + dto.getBlockId()));
            // 프로젝트 일관성 확인(다르면 예외)
            if (existing.getProject() != null && project != null && !existing.getProject().getId().equals(project.getId())) {
                throw new IllegalArgumentException("Expression block does not belong to project: " + dto.getBlockId());
            }
            return existing;
        }

        String type = dto.getValueType();
        ExpressionBlock expressionBlock;
        if ("LITERAL".equalsIgnoreCase(type)) {
            Object data = dto.getData();
            String literalType = resolveLiteralType(data);
            expressionBlock = new LiteralExpressionBlock(data == null ? null : data.toString(), literalType);
        } else if ("VARIABLE".equalsIgnoreCase(type)) {
            Long varId = dto.getVariableId();
            String varName = dto.getVariableName();
            if (varId == null) {
                Variable var = resolveVariable(project, null, varName);
                varId = var.getId();
                varName = var.getName();
            }
            expressionBlock = new VariableExpressionBlock(varId, varName);
        } else if ("UNARY".equalsIgnoreCase(type)) {
            expressionBlock = new UnaryExpressionBlock(dto.getOperator(), buildExpression(dto.getOperand(), project));
        } else if ("BINARY".equalsIgnoreCase(type)) {
            expressionBlock = new BinaryExpressionBlock(dto.getOperator(),
                    buildExpression(dto.getLeft(), project),
                    buildExpression(dto.getRight(), project));
        } else {
            throw new IllegalArgumentException("Unknown valueType: " + type);
        }

        expressionBlock.setProject(project);
        return expressionBlock;
    }

    private String resolveLiteralType(Object data) {
        if (data instanceof Boolean) {
            return "BOOLEAN";
        }
        if (data instanceof Number) {
            return "NUMBER";
        }
        return "STRING";
    }

    private Variable createOrGetVariable(Project project, String name, String type) {
        if (project == null) throw new IllegalArgumentException("Project required");
        if (name == null || type == null) throw new IllegalArgumentException("variableName and variableType required");
        return variableRepository.findByProjectIdAndName(project.getId(), name)
                .orElseGet(() -> variableRepository.save(
                        Variable.builder()
                                .name(name)
                                .type(type)
                                .project(project)
                                .build()
                ));
    }

    private Variable resolveVariable(Project project, Long variableId, String variableName) {
        if (variableId != null) {
            return variableRepository.findById(variableId)
                    .orElseThrow(() -> new IllegalArgumentException("Variable not found: " + variableId));
        }
        if (variableName != null) {
            return variableRepository.findByProjectIdAndName(project.getId(), variableName)
                    .orElseThrow(() -> new IllegalArgumentException("Variable not found: " + variableName));
        }
        throw new IllegalArgumentException("variableId or variableName is required");
    }

}
