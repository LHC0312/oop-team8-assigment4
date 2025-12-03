package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team8.dto.BlockDto;
import team8.dto.ProjectCreateRequest;
import team8.dto.ProjectDto;
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
import team8.model.output.PrintBlock;
import team8.model.variable.VariableAssignBlock;
import team8.model.variable.VariableDeclareBlock;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;
import team8.service.BlockServiceHelper;

import java.util.List;
import java.util.stream.Collectors;
import team8.service.BlockServiceHelper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final BlockRepository blockRepository;

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
        } else if (block instanceof SubtractBlock subtractBlock) {
            dto.setOperand1(BlockServiceHelper.toValueDto(subtractBlock.getOperand1Block()));
            dto.setOperand2(BlockServiceHelper.toValueDto(subtractBlock.getOperand2Block()));
            dto.setResultVariable(subtractBlock.getResultVariable());
        } else if (block instanceof MultiplyBlock multiplyBlock) {
            dto.setOperand1(BlockServiceHelper.toValueDto(multiplyBlock.getOperand1Block()));
            dto.setOperand2(BlockServiceHelper.toValueDto(multiplyBlock.getOperand2Block()));
            dto.setResultVariable(multiplyBlock.getResultVariable());
        } else if (block instanceof DivideBlock divideBlock) {
            dto.setOperand1(BlockServiceHelper.toValueDto(divideBlock.getOperand1Block()));
            dto.setOperand2(BlockServiceHelper.toValueDto(divideBlock.getOperand2Block()));
            dto.setResultVariable(divideBlock.getResultVariable());
        } else if (block instanceof VariableDeclareBlock declareBlock) {
            dto.setVariableName(declareBlock.getVariableName());
            dto.setVariableType(declareBlock.getVariableType());
            dto.setInitial(BlockServiceHelper.toValueDto(declareBlock.getInitialExpressionBlock()));
        } else if (block instanceof VariableAssignBlock assignBlock) {
            dto.setVariableName(assignBlock.getVariableName());
            dto.setValue(BlockServiceHelper.toValueDto(assignBlock.getValueExpressionBlock()));
        }

        return dto;
    }
}
