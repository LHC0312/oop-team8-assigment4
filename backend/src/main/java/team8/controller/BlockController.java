package team8.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team8.dto.BlockCreateRequest;
import team8.dto.BlockDto;
import team8.service.BlockService;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Tag(name = "Block", description = "블록 관리 API")
public class BlockController {

    private final BlockService blockService;

    @GetMapping("/project/{projectId}")
    @Operation(summary = "프로젝트의 블록 조회", description = "특정 프로젝트에 속한 모든 블록을 조회합니다.")
    public ResponseEntity<List<BlockDto>> getBlocksByProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        return ResponseEntity.ok(blockService.getBlocksByProjectId(projectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "블록 조회", description = "ID로 특정 블록을 조회합니다.")
    public ResponseEntity<BlockDto> getBlock(
            @Parameter(description = "블록 ID") @PathVariable Long id) {
        return ResponseEntity.ok(blockService.getBlockById(id));
    }

    @PostMapping("/project/{projectId}")
    @Operation(summary = "블록 생성", description = "프로젝트에 새로운 블록을 추가합니다.")
    public ResponseEntity<BlockDto> createBlock(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @RequestBody BlockCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blockService.createBlock(projectId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "블록 수정", description = "기존 블록을 수정합니다.")
    public ResponseEntity<BlockDto> updateBlock(
            @Parameter(description = "블록 ID") @PathVariable Long id,
            @RequestBody BlockCreateRequest request) {
        return ResponseEntity.ok(blockService.updateBlock(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "블록 삭제", description = "블록을 삭제합니다.")
    public ResponseEntity<Void> deleteBlock(
            @Parameter(description = "블록 ID") @PathVariable Long id) {
        blockService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }
}
