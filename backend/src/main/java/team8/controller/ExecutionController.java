package team8.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team8.service.BlockExecutionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
@Tag(name = "Execution", description = "블록 프로그램 실행 API")
public class ExecutionController {

    private final BlockExecutionService executionService;

    @PostMapping("/projects/{projectId}/run")
    @Operation(summary = "프로젝트 실행", description = "프로젝트의 블록 프로그램을 실행합니다. WebSocket으로 실시간 결과를 받을 수 있습니다.")
    public ResponseEntity<Map<String, String>> runProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {

        String sessionId = UUID.randomUUID().toString();

        new Thread(() -> {
            try {
                executionService.executeProject(projectId, sessionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("message", "Execution started. Subscribe to /topic/execution/" + sessionId + " for real-time output");

        return ResponseEntity.ok(response);
    }
}
