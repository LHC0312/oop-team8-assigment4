package team8.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team8.dto.ProjectCreateRequest;
import team8.dto.ProjectDto;
import team8.model.Project;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private BlockRepository blockRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .blocks(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("모든 프로젝트 조회 테스트")
    void testGetAllProjects() {
        when(projectRepository.findAll()).thenReturn(Arrays.asList(testProject));

        List<ProjectDto> projects = projectService.getAllProjects();

        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertEquals("Test Project", projects.get(0).getName());

        verify(projectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("프로젝트 ID로 조회 테스트")
    void testGetProjectById() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        ProjectDto project = projectService.getProjectById(1L);

        assertNotNull(project);
        assertEquals(1L, project.getId());
        assertEquals("Test Project", project.getName());

        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 조회 시 예외 발생")
    void testGetProjectByIdNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            projectService.getProjectById(999L);
        });

        verify(projectRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("프로젝트 생성 테스트")
    void testCreateProject() {
        ProjectCreateRequest request = new ProjectCreateRequest("New Project", "New Description");

        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        ProjectDto created = projectService.createProject(request);

        assertNotNull(created);
        assertEquals("Test Project", created.getName());

        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 수정 테스트")
    void testUpdateProject() {
        ProjectCreateRequest request = new ProjectCreateRequest("Updated Project", "Updated Description");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        ProjectDto updated = projectService.updateProject(1L, request);

        assertNotNull(updated);
        assertEquals("Updated Project", updated.getName());
        assertEquals("Updated Description", updated.getDescription());

        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("프로젝트 삭제 테스트")
    void testDeleteProject() {
        doNothing().when(projectRepository).deleteById(1L);

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("프로젝트 검색 테스트")
    void testSearchProjects() {
        when(projectRepository.findByNameContaining("Test")).thenReturn(Arrays.asList(testProject));

        List<ProjectDto> results = projectService.searchProjects("Test");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Project", results.get(0).getName());

        verify(projectRepository, times(1)).findByNameContaining("Test");
    }
}
