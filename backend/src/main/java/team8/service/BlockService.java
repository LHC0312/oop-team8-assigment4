package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team8.dto.BlockConnectRequest;
import team8.dto.BlockCreateRequest;
import team8.dto.BlockDto;
import team8.dto.ExpressionConnectRequest;
import team8.dto.ValueDto;
import team8.model.Block;
import team8.model.Project;
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
import java.util.Collections;
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
        deleteBlockRecursive(id, new java.util.HashSet<>());
    }

    private void deleteBlockRecursive(Long blockId, java.util.Set<Long> visited) {
        if (blockId == null || visited.contains(blockId)) {
            return;
        }
        visited.add(blockId);

        Block block = blockRepository.findById(blockId).orElse(null);
        if (block == null) {
            return;
        }

        // 표현식 트리 삭제
        deleteExpressionsOfBlock(block);

        // 다음 블록/분기 블록 재귀 삭제
        Long next = block.getNextBlockId();
        if (block instanceof ControlBlock controlBlock) {
            deleteBlockRecursive(controlBlock.getTrueBranchId(), visited);
            deleteBlockRecursive(controlBlock.getFalseBranchId(), visited);
        }
        blockRepository.delete(block);
        deleteBlockRecursive(next, visited);
    }

    @Transactional
    public List<ValueDto> connectExpressions(Long projectId, List<ExpressionConnectRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        for (ExpressionConnectRequest req : requests) {
            if (req.getExpressionId() == null) {
                continue;
            }
            ExpressionBlock expression = expressionRepository.findById(req.getExpressionId())
                    .orElseThrow(() -> new RuntimeException("Expression not found: " + req.getExpressionId()));
            if (expression.getProject() != null && !projectId.equals(expression.getProject().getId())) {
                throw new IllegalArgumentException("Expression " + req.getExpressionId() + " does not belong to project " + projectId);
            }

            if (expression instanceof UnaryExpressionBlock unary) {
                if (req.getOperandExpressionId() != null) {
                    ExpressionBlock operand = expressionRepository.findById(req.getOperandExpressionId())
                            .orElseThrow(() -> new RuntimeException("Operand expression not found: " + req.getOperandExpressionId()));
                    if (operand.getProject() != null && !projectId.equals(operand.getProject().getId())) {
                        throw new IllegalArgumentException("Operand expression " + req.getOperandExpressionId() + " does not belong to project " + projectId);
                    }
                    unary.setOperand(operand);
                }
            } else if (expression instanceof BinaryExpressionBlock binary) {
                if (req.getLeftExpressionId() != null) {
                    ExpressionBlock left = expressionRepository.findById(req.getLeftExpressionId())
                            .orElseThrow(() -> new RuntimeException("Left expression not found: " + req.getLeftExpressionId()));
                    if (left.getProject() != null && !projectId.equals(left.getProject().getId())) {
                        throw new IllegalArgumentException("Left expression " + req.getLeftExpressionId() + " does not belong to project " + projectId);
                    }
                    binary.setLeft(left);
                }
                if (req.getRightExpressionId() != null) {
                    ExpressionBlock right = expressionRepository.findById(req.getRightExpressionId())
                            .orElseThrow(() -> new RuntimeException("Right expression not found: " + req.getRightExpressionId()));
                    if (right.getProject() != null && !projectId.equals(right.getProject().getId())) {
                        throw new IllegalArgumentException("Right expression " + req.getRightExpressionId() + " does not belong to project " + projectId);
                    }
                    binary.setRight(right);
                }
            }
        }

        return expressionRepository.findAll().stream()
                .filter(expr -> expr.getProject() != null && projectId.equals(expr.getProject().getId()))
                .map(expr -> BlockServiceHelper.toValueDto(expr, this::loadExpressionById))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BlockDto> connectBlocks(Long projectId, List<BlockConnectRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return getBlocksByProjectId(projectId);
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        for (BlockConnectRequest req : requests) {
            if (req.getBlockId() == null) {
                continue;
            }
            Block block = blockRepository.findById(req.getBlockId())
                    .orElseThrow(() -> new RuntimeException("Block not found: " + req.getBlockId()));
            if (!projectId.equals(block.getProject().getId())) {
                throw new IllegalArgumentException("Block " + req.getBlockId() + " does not belong to project " + projectId);
            }

            if (req.getNextBlockId() != null) {
                block.setNextBlockId(req.getNextBlockId());
            }
            if (block instanceof ControlBlock control) {
                if (req.getTrueBranchId() != null) {
                    control.setTrueBranchId(req.getTrueBranchId());
                }
                if (req.getFalseBranchId() != null) {
                    control.setFalseBranchId(req.getFalseBranchId());
                }
            }
        }

        // 간단히 전체 프로젝트 블록을 DTO로 반환해 프론트가 최신 상태를 얻도록 함.
        return getBlocksByProjectId(projectId);
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
            dto.setCondition(BlockServiceHelper.toValueDto(controlBlock.getConditionExpressionBlock(), this::loadExpressionById));
            dto.setTrueBranchId(controlBlock.getTrueBranchId());
            dto.setFalseBranchId(controlBlock.getFalseBranchId());

            if (block instanceof ForBlock forBlock) {
                dto.setInit(BlockServiceHelper.toValueDto(forBlock.getInitExpressionBlock(), this::loadExpressionById));
                dto.setIncrement(BlockServiceHelper.toValueDto(forBlock.getIncrementExpressionBlock(), this::loadExpressionById));
            }
        } else if (block instanceof PrintBlock printBlock) {
            dto.setMessage(BlockServiceHelper.toValueDto(printBlock.getMessageExpressionBlock(), this::loadExpressionById));
        } else if (block instanceof VariableDeclareBlock declareBlock) {
            dto.setVariableName(declareBlock.getVariableName());
            dto.setVariableType(declareBlock.getVariableType());
            dto.setVariableId(declareBlock.getVariableId());
            dto.setInitial(BlockServiceHelper.toValueDto(declareBlock.getInitialExpressionBlock(), this::loadExpressionById));
        } else if (block instanceof VariableAssignBlock assignBlock) {
            dto.setVariableName(assignBlock.getVariableName());
            dto.setVariableId(assignBlock.getVariableId());
            dto.setValue(BlockServiceHelper.toValueDto(assignBlock.getValueExpressionBlock(), this::loadExpressionById));
        }

        return dto;
    }

    private ExpressionBlock loadExpressionById(Long expressionId) {
        if (expressionId == null) {
            return null;
        }
        return expressionRepository.findById(expressionId)
                .orElseThrow(() -> new IllegalArgumentException("Expression not found: " + expressionId));
    }

    @Transactional
    public ValueDto createExpression(Long projectId, ValueDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        ExpressionBlock expression = buildExpression(dto, project);
        return BlockServiceHelper.toValueDto(expression, this::loadExpressionById);
    }

    @Transactional
    public void deleteExpression(Long expressionId) {
        ExpressionBlock expression = expressionRepository.findById(expressionId)
                .orElseThrow(() -> new IllegalArgumentException("Expression not found: " + expressionId));
        deleteExpressionRecursive(expression, new java.util.HashSet<>());
    }

    @Transactional(readOnly = true)
    public List<ValueDto> getExpressionsByProject(Long projectId) {
        return expressionRepository.findAll().stream()
                .filter(expr -> expr.getProject() != null && projectId.equals(expr.getProject().getId()))
                .map(expr -> BlockServiceHelper.toValueDto(expr, this::loadExpressionById))
                .collect(Collectors.toList());
    }

    private void applyPosition(ExpressionBlock expressionBlock, ValueDto dto) {
        if (expressionBlock == null || dto == null) {
            return;
        }
        if (dto.getPositionX() != null) {
            expressionBlock.setPositionX(dto.getPositionX());
        }
        if (dto.getPositionY() != null) {
            expressionBlock.setPositionY(dto.getPositionY());
        }
    }

    private ExpressionBlock resolveExpression(ExpressionBlock expressionBlock, Long expressionId) {
        if (expressionBlock != null) {
            return expressionBlock;
        }
        if (expressionId != null) {
            return loadExpressionById(expressionId);
        }
        return null;
    }

    private ExpressionBlock buildExpression(ValueDto dto, Project project) {
        if (dto == null) {
            return null;
        }

        if (dto.getBlockId() != null) {
            ExpressionBlock existing = expressionRepository.findById(dto.getBlockId()).orElse(null);
            if (existing != null) {
                // 프로젝트 일관성 확인(다르면 예외)
                if (existing.getProject() != null && project != null && !existing.getProject().getId().equals(project.getId())) {
                    throw new IllegalArgumentException("Expression block does not belong to project: " + dto.getBlockId());
                }
                applyPosition(existing, dto);
                return existing;
            }
            if (dto.getValueType() == null) {
                throw new IllegalArgumentException("Expression not found: " + dto.getBlockId());
            }
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
            ExpressionBlock operand = buildExpression(dto.getOperand(), project);
            UnaryExpressionBlock unary = new UnaryExpressionBlock(dto.getOperator(),
                    operand == null ? null : operand.getId());
            unary.setOperand(operand);
            expressionBlock = unary;
        } else if ("BINARY".equalsIgnoreCase(type)) {
            ExpressionBlock left = buildExpression(dto.getLeft(), project);
            ExpressionBlock right = buildExpression(dto.getRight(), project);
            BinaryExpressionBlock binary = new BinaryExpressionBlock(dto.getOperator(),
                    left == null ? null : left.getId(),
                    right == null ? null : right.getId());
            binary.setLeft(left);
            binary.setRight(right);
            expressionBlock = binary;
        } else {
            throw new IllegalArgumentException("Unknown valueType: " + type);
        }

        expressionBlock.setProject(project);
        applyPosition(expressionBlock, dto);
        return expressionRepository.save(expressionBlock);
    }

    private void deleteExpressionsOfBlock(Block block) {
        java.util.Set<Long> visited = new java.util.HashSet<>();
        if (block instanceof team8.model.control.ControlBlock control) {
            deleteExpressionRecursive(resolveExpression(control.getConditionExpressionBlock(),
                    control.getConditionExpressionBlock() != null ? control.getConditionExpressionBlock().getId() : null), visited);
        }
        if (block instanceof team8.model.output.PrintBlock print) {
            deleteExpressionRecursive(resolveExpression(print.getMessageExpressionBlock(),
                    print.getMessageExpressionBlock() != null ? print.getMessageExpressionBlock().getId() : null), visited);
        }
        if (block instanceof team8.model.variable.VariableDeclareBlock declare) {
            deleteExpressionRecursive(resolveExpression(declare.getInitialExpressionBlock(),
                    declare.getInitialExpressionBlock() != null ? declare.getInitialExpressionBlock().getId() : null), visited);
        }
        if (block instanceof team8.model.variable.VariableAssignBlock assign) {
            deleteExpressionRecursive(resolveExpression(assign.getValueExpressionBlock(),
                    assign.getValueExpressionBlock() != null ? assign.getValueExpressionBlock().getId() : null), visited);
        }
    }

    private void deleteExpressionRecursive(ExpressionBlock expressionBlock, java.util.Set<Long> visited) {
        if (expressionBlock == null) {
            return;
        }
        Long exprId = expressionBlock.getId();
        if (exprId != null && visited.contains(exprId)) {
            return;
        }
        if (exprId != null) {
            visited.add(exprId);
        }

        if (expressionBlock instanceof UnaryExpressionBlock unary) {
            ExpressionBlock operand = resolveExpression(unary.getOperand(), unary.getOperandExpressionId());
            deleteExpressionRecursive(operand, visited);
        } else if (expressionBlock instanceof BinaryExpressionBlock binary) {
            ExpressionBlock left = resolveExpression(binary.getLeft(), binary.getLeftExpressionId());
            ExpressionBlock right = resolveExpression(binary.getRight(), binary.getRightExpressionId());
            deleteExpressionRecursive(left, visited);
            deleteExpressionRecursive(right, visited);
        }
        expressionRepository.delete(expressionBlock);
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
