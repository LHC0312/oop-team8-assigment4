package team8.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team8.service.BlockExecutionService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
@Tag(name = "Execution", description = "블록 프로그램 실행 API")
public class ExecutionController {

    private final BlockExecutionService executionService;

    @PostMapping("/projects/{projectId}/run")
    @Operation(summary = "프로젝트 실행", description = "프로젝트의 블록 프로그램을 실행합니다. WebSocket으로 실시간 결과를 받을 수 있습니다.")
    public ResponseEntity<Map<String, String>> runProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @RequestParam(name = "debug", defaultValue = "false") boolean debugMode,
            @RequestParam(name = "trace", defaultValue = "false") boolean traceEnabled) {

        String sessionId = executionService.startExecution(projectId, debugMode, traceEnabled);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("message", debugMode
                ? "Debug execution started. Call /api/execution/sessions/" + sessionId + "/step for the next block"
                : "Execution started. Subscribe to /topic/execution/" + sessionId + " for real-time output");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/projects/{projectId}/run/normal")
    @Operation(summary = "프로젝트 실행 (기본)", description = "trace/debug 없이 기본 실행을 시작합니다.")
    public ResponseEntity<Map<String, String>> runProjectNormal(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        return runProject(projectId, false, false);
    }

    @PostMapping("/projects/{projectId}/run/trace")
    @Operation(summary = "프로젝트 실행 (트레이스)", description = "trace=true로 실행하여 각 블록 경로를 WebSocket TRACE 메시지로 확인합니다.")
    public ResponseEntity<Map<String, String>> runProjectTrace(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        return runProject(projectId, false, true);
    }

    @PostMapping("/sessions/{sessionId}/stop")
    @Operation(summary = "프로젝트 실행 중지", description = "현재 실행 중인 블록 프로그램을 중지합니다.")
    public ResponseEntity<Map<String, String>> stopProject(
            @Parameter(description = "실행 세션 ID") @PathVariable String sessionId) {

        boolean stopped = executionService.stopExecution(sessionId);

        if (!stopped) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "sessionId", sessionId,
                            "message", "Session not found or already finished"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "message", "Stop requested"
        ));
    }

    @PostMapping("/sessions/{sessionId}/step")
    @Operation(summary = "디버그 한 스텝 실행", description = "디버그 모드로 실행 중인 세션에서 다음 블록을 실행합니다.")
    public ResponseEntity<Map<String, String>> stepProject(
            @Parameter(description = "실행 세션 ID") @PathVariable String sessionId) {

        BlockExecutionService.StepResult result = executionService.stepExecution(sessionId);

        if (result == BlockExecutionService.StepResult.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "sessionId", sessionId,
                            "message", "Session not found"
                    ));
        }

        if (result == BlockExecutionService.StepResult.NOT_IN_DEBUG_MODE) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "sessionId", sessionId,
                            "message", "Session is not running in debug mode"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "message", "Stepped to next block"
        ));
    }

    @GetMapping("/sessions/{sessionId}/variables")
    @Operation(summary = "실행 컨텍스트 변수 조회", description = "현재 실행 세션의 변수 맵을 조회합니다.")
    public ResponseEntity<?> getVariables(
            @Parameter(description = "실행 세션 ID") @PathVariable String sessionId) {

        Map<String, Object> vars = executionService.getSessionVariables(sessionId);
        if (vars == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found"));
        }
        return ResponseEntity.ok(vars);
    }
}
