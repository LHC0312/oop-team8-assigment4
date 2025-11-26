package team8.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team8.dto.ProjectCreateRequest;
import team8.dto.ProjectDto;
import team8.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "블록 코딩 프로젝트 관리 API")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "모든 프로젝트 조회", description = "저장된 모든 프로젝트 목록을 조회합니다.")
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "프로젝트 조회", description = "ID로 특정 프로젝트를 조회합니다.")
    public ResponseEntity<ProjectDto> getProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "프로젝트 검색", description = "키워드로 프로젝트를 검색합니다.")
    public ResponseEntity<List<ProjectDto>> searchProjects(
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {
        return ResponseEntity.ok(projectService.searchProjects(keyword));
    }

    @PostMapping
    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    public ResponseEntity<ProjectDto> createProject(@RequestBody ProjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "프로젝트 수정", description = "기존 프로젝트를 수정합니다.")
    public ResponseEntity<ProjectDto> updateProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long id,
            @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
