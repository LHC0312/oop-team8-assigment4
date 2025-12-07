package team8.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import team8.execution.OutputMessage;
import team8.model.Block;
import team8.model.Project;
import team8.model.control.WhileBlock;
import team8.model.output.PrintBlock;
import team8.model.start.StartBlock;
import team8.model.variable.VariableDeclareBlock;
import team8.repository.BlockRepository;
import team8.repository.ExpressionRepository;
import team8.repository.ProjectRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class BlockExecutionServiceTest {

    private BlockRepository blockRepository;
    private ProjectRepository projectRepository;
    private ExpressionRepository expressionRepository;
    private SimpMessagingTemplate messagingTemplate;
    private BlockingQueue<OutputMessage> messages;

    @BeforeEach
    void setUp() {
        blockRepository = Mockito.mock(BlockRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        expressionRepository = Mockito.mock(ExpressionRepository.class);
        messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        messages = new LinkedBlockingQueue<>();

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(Project.builder().id(1L).name("Test").build()));

        doAnswer(invocation -> {
            OutputMessage message = invocation.getArgument(1);
            messages.offer(message);
            return null;
        }).when(messagingTemplate).convertAndSend(anyString(), Mockito.<Object>any());
    }

    @Test
    @DisplayName("디버그 모드에서 스텝 실행 시 분기 내부 마지막 블록의 fallback 연결로 반복 실행된다")
    void debugStepLoopsWithFallbackAndCompletes() throws Exception {
        when(blockRepository.findByProjectIdOrderByOrderAsc(1L))
                .thenReturn(buildWhileBlocks());

        BlockExecutionService service = new BlockExecutionService(
                blockRepository, projectRepository, expressionRepository, messagingTemplate);

        String sessionId = service.startExecution(1L, true, true);

        int assignCount = 0;
        boolean completed = false;
        int safety = 0;

        while (!completed && safety++ < 30) {
            OutputMessage message = messages.poll(3, TimeUnit.SECONDS);
            assertNotNull(message, "메시지를 받지 못했습니다");

            switch (message.getType()) {
                case "DEBUG_WAIT" -> assertEquals(
                        BlockExecutionService.StepResult.STEPPED,
                        service.stepExecution(sessionId));
                case "VAR_ASSIGN" -> assignCount++;
                case "COMPLETE" -> completed = true;
                case "ERROR" -> fail("ERROR 발생: " + message.getData());
                default -> { }
            }
        }

        assertTrue(completed, "완료 메시지를 받지 못했습니다");
        assertEquals(2, assignCount, "증가 블록이 반복 실행되지 않았습니다");
    }

    @Test
    @DisplayName("실행 중지 요청 시 STOPPED 이벤트가 전송된다")
    void stopExecutionSendsStoppedEvent() throws Exception {
        when(blockRepository.findByProjectIdOrderByOrderAsc(1L))
                .thenReturn(buildSimpleBlocks());

        BlockExecutionService service = new BlockExecutionService(
                blockRepository, projectRepository, expressionRepository, messagingTemplate);

        String sessionId = service.startExecution(1L, true, true);

        OutputMessage firstWait = waitForType("DEBUG_WAIT");
        assertEquals("DEBUG_WAIT", firstWait.getType());

        assertTrue(service.stopExecution(sessionId), "세션 중지 실패");

        OutputMessage stopped = waitForType("STOPPED");
        assertEquals("STOPPED", stopped.getType());
    }

    private List<Block> buildWhileBlocks() {
        StartBlock start = StartBlock.builder()
                .id(1L)
                .nextBlockId(2L)
                .build();

        VariableDeclareBlock declare = VariableDeclareBlock.builder()
                .id(2L)
                .variableId(20L)
                .variableName("x")
                .variableType("number")
                .initialExpressionBlock(team8.model.expression.LiteralExpressionBlock.builder().value("0").literalType("NUMBER").build())
                .nextBlockId(3L)
                .build();

        WhileBlock whileBlock = WhileBlock.builder()
                .id(3L)
                .conditionExpressionBlock(team8.model.expression.BinaryExpressionBlock.builder()
                        .operator("<")
                        .left(team8.model.expression.VariableExpressionBlock.builder().variableName("x").build())
                        .right(team8.model.expression.LiteralExpressionBlock.builder().value("2").literalType("NUMBER").build())
                        .build())
                .trueBranchId(4L)
                .nextBlockId(null) // false 시 종료
                .build();

        team8.model.variable.VariableAssignBlock assign = team8.model.variable.VariableAssignBlock.builder()
                .id(4L)
                .variableName("x")
                .variableId(20L)
                .valueExpressionBlock(team8.model.expression.BinaryExpressionBlock.builder()
                        .operator("+")
                        .left(team8.model.expression.VariableExpressionBlock.builder().variableName("x").build())
                        .right(team8.model.expression.LiteralExpressionBlock.builder().value("1").literalType("NUMBER").build())
                        .build())
                .nextBlockId(null) // fallback으로 while로 돌아가야 함
                .build();

        return Arrays.asList(start, declare, whileBlock, assign);
    }

    private List<Block> buildSimpleBlocks() {
        StartBlock start = StartBlock.builder()
                .id(1L)
                .nextBlockId(2L)
                .build();

        PrintBlock print = PrintBlock.builder()
                .id(2L)
                .messageExpressionBlock(team8.model.expression.LiteralExpressionBlock.builder().value("hello").literalType("STRING").build())
                .nextBlockId(null)
                .build();

        return Arrays.asList(start, print);
    }

    private OutputMessage waitForType(String type) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            OutputMessage message = messages.poll(1, TimeUnit.SECONDS);
            if (message != null && type.equals(message.getType())) {
                return message;
            }
        }
        fail(type + " 메시지를 받지 못했습니다");
        return null; // unreachable
    }
}
