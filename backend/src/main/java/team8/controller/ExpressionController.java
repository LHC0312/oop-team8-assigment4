package team8.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import team8.dto.ExpressionConnectRequest;
import team8.dto.ValueDto;
import team8.service.BlockService;

import java.util.List;

@RestController
@RequestMapping("/api/expressions")
@RequiredArgsConstructor
@Tag(name = "Expression", description = "표현식(값) 블록 연결 API")
public class ExpressionController {

    private final BlockService blockService;

    @PostMapping("/project/{projectId}/connect")
    @Operation(summary = "표현식 연결 갱신", description = "UNARY/BINARY 표현식의 자식 표현식 ID를 한번에 갱신합니다.")
    public ResponseEntity<List<ValueDto>> connectExpressions(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @RequestBody List<ExpressionConnectRequest> requests) {
        return ResponseEntity.ok(blockService.connectExpressions(projectId, requests));
    }

    @PostMapping("/project/{projectId}")
    @Operation(summary = "표현식 생성", description = "ValueDto 트리로 표현식을 생성하고 ID를 반환합니다.")
    public ResponseEntity<ValueDto> createExpression(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @RequestBody ValueDto dto) {
        return ResponseEntity.ok(blockService.createExpression(projectId, dto));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "프로젝트의 표현식 조회", description = "연결 여부와 상관없이 프로젝트 내 모든 표현식을 조회합니다.")
    public ResponseEntity<List<ValueDto>> getExpressions(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        return ResponseEntity.ok(blockService.getExpressionsByProject(projectId));
    }

    @DeleteMapping("/{expressionId}")
    @Operation(summary = "표현식 삭제", description = "표현식 ID를 기반으로 표현식을 삭제합니다.")
    public ResponseEntity<Void> deleteExpression(
            @Parameter(description = "표현식 ID") @PathVariable Long expressionId) {
        blockService.deleteExpression(expressionId);
        return ResponseEntity.noContent().build();
    }
}
