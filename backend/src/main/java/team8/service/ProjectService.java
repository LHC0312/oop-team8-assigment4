package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team8.dto.BlockDto;
import team8.dto.ProjectCreateRequest;
import team8.dto.ProjectDto;
import team8.model.Block;
import team8.model.Project;
import team8.model.control.ControlBlock;
import team8.model.control.ForBlock;
import team8.model.control.IfBlock;
import team8.model.control.WhileBlock;
import team8.model.output.PrintBlock;
import team8.model.variable.VariableAssignBlock;
import team8.model.variable.VariableDeclareBlock;
import team8.repository.BlockRepository;
import team8.repository.ExpressionRepository;
import team8.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final BlockRepository blockRepository;
    private final ExpressionRepository expressionRepository;

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToDto(project);
    }

    @Transactional
    public ProjectDto createProject(ProjectCreateRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }

    @Transactional
    public ProjectDto updateProject(Long id, ProjectCreateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        return convertToDto(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public List<ProjectDto> searchProjects(String keyword) {
        return projectRepository.findByNameContaining(keyword).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private team8.model.expression.ExpressionBlock loadExpressionById(Long expressionId) {
        if (expressionId == null) {
            return null;
        }
        return expressionRepository.findById(expressionId)
                .orElseThrow(() -> new IllegalArgumentException("Expression not found: " + expressionId));
    }

    private ProjectDto convertToDto(Project project) {
        List<BlockDto> blockDtos = project.getBlocks().stream()
                .map(this::convertBlockToDto)
                .collect(Collectors.toList());

        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .blocks(blockDtos)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private BlockDto convertBlockToDto(Block block) {
        BlockDto dto = BlockDto.builder()
                .id(block.getId())
                .blockType(block.getBlockType())
                .positionX(block.getPositionX())
                .positionY(block.getPositionY())
                .order(block.getOrder())
                .nextBlockId(block.getNextBlockId())
                .build();

        if (block instanceof ControlBlock controlBlock) {
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
            dto.setInitial(BlockServiceHelper.toValueDto(declareBlock.getInitialExpressionBlock(), this::loadExpressionById));
        } else if (block instanceof VariableAssignBlock assignBlock) {
            dto.setVariableName(assignBlock.getVariableName());
            dto.setValue(BlockServiceHelper.toValueDto(assignBlock.getValueExpressionBlock(), this::loadExpressionById));
        }

        return dto;
    }
}
