package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team8.dto.BlockDto;
import team8.dto.ProjectCreateRequest;
import team8.dto.ProjectDto;
import team8.model.Block;
import team8.model.Project;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

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
        return BlockDto.builder()
                .id(block.getId())
                .blockType(block.getBlockType())
                .positionX(block.getPositionX())
                .positionY(block.getPositionY())
                .order(block.getOrder())
                .nextBlockId(block.getNextBlockId())
                .build();
    }
}
