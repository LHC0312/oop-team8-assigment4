package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;
import team8.model.Block;
import team8.model.arithmetic.ArithmeticBlock;
import team8.model.control.ControlBlock;
import team8.model.control.ForBlock;
import team8.model.control.WhileBlock;
import team8.model.expression.BinaryExpressionBlock;
import team8.model.expression.ExpressionBlock;
import team8.model.expression.UnaryExpressionBlock;
import team8.model.start.StartBlock;
import team8.model.variable.VariableAssignBlock;
import team8.model.variable.VariableDeclareBlock;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class BlockExecutionService {

    private final BlockRepository blockRepository;
    private final ProjectRepository projectRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, ExecutionSession> sessions = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public String startExecution(Long projectId, boolean debugMode, boolean traceEnabled) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Block> blocks = blockRepository.findByProjectIdOrderByOrderAsc(projectId);

        // Lazy 로딩된 표현식을 트랜잭션 안에서 미리 초기화해 새 스레드에서 LazyInitializationException이 나지 않도록 한다.
        preloadExpressionBlocks(blocks);

        Map<Long, Block> blockMap = new HashMap<>();
        Block startBlock = null;

        for (Block block : blocks) {
            blockMap.put(block.getId(), block);
            if (block instanceof StartBlock) {
                startBlock = block;
            }
        }

        if (startBlock == null) {
            throw new RuntimeException("No start block found");
        }

        String sessionId = UUID.randomUUID().toString();
        Map<Long, Long> fallbackConnections = buildFallbackConnections(blockMap);
        ExecutionContext context = new ExecutionContext(messagingTemplate, sessionId, debugMode, traceEnabled);
        ExecutionSession session = new ExecutionSession(sessionId, context, blockMap, fallbackConnections);
        sessions.put(sessionId, session);

        final Block initialBlock = startBlock;
        Thread thread = new Thread(() -> runSession(session, initialBlock));
        session.runnerThread = thread;
        thread.start();

        return sessionId;
    }

    public boolean stopExecution(String sessionId) {
        ExecutionSession session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }

        session.context.stop();
        releaseStep(session);

        if (session.runnerThread != null) {
            session.runnerThread.interrupt();
        }
        return true;
    }

    public StepResult stepExecution(String sessionId) {
        ExecutionSession session = sessions.get(sessionId);
        if (session == null) {
            return StepResult.NOT_FOUND;
        }
        if (!session.context.isDebugMode()) {
            return StepResult.NOT_IN_DEBUG_MODE;
        }

        releaseStep(session);
        return StepResult.STEPPED;
    }

    public Map<String, Object> getSessionVariables(String sessionId) {
        ExecutionSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        return session.context.getVariablesSnapshot();
    }

    private void runSession(ExecutionSession session, Block startBlock) {
        try {
            // 구독 준비 시간 확보 (웹소켓 구독이 HTTP 응답 후 시작되므로 약간의 지연을 둔다)
            try {
                Thread.sleep(120);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            executeFromBlock(startBlock, session);
            if (session.context.isStopped()) {
                session.context.sendOutput("STOPPED", "Execution stopped by request");
            } else {
                session.context.sendOutput("COMPLETE", "Execution completed successfully");
            }
        } catch (Exception e) {
            session.context.sendOutput("ERROR", "Execution error: " + e.getMessage());
        } finally {
            sessions.remove(session.sessionId);
        }
    }

    private void executeFromBlock(Block currentBlock, ExecutionSession session) {
        int maxIterations = 10000;
        int iterations = 0;

        while (currentBlock != null && !session.context.isStopped()) {
            if (iterations++ > maxIterations) {
                throw new RuntimeException("Maximum iteration limit reached (possible infinite loop)");
            }

            try {
                Thread.sleep(20); // 클라이언트가 메시지를 수신/그리기 할 수 있도록 약간의 지연
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            session.context.sendTrace("TRACE", Map.of(
                    "blockId", currentBlock.getId(),
                    "blockType", currentBlock.getBlockType()
            ));

            waitForStepIfNeeded(session, currentBlock);

            if (session.context.isStopped()) {
                break;
            }

            ExecutionResult result = currentBlock.execute(session.context);

            Long nextBlockId = result.getNextBlockId();
            if (nextBlockId == null) {
                nextBlockId = session.fallbackNextMap.get(currentBlock.getId());
            }

            if (nextBlockId == null) {
                break;
            }

            currentBlock = session.blockMap.get(nextBlockId);
        }
    }

    private void waitForStepIfNeeded(ExecutionSession session, Block currentBlock) {
        if (!session.context.isDebugMode()) {
            return;
        }

        session.stepLock.lock();
        try {
            session.waitingForStep = true;
            session.context.sendDebug("DEBUG_WAIT", Map.of(
                    "blockId", currentBlock.getId(),
                    "blockType", currentBlock.getBlockType()
            ));

            while (session.waitingForStep && !session.context.isStopped()) {
                session.stepCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            session.stepLock.unlock();
        }
    }

    private void releaseStep(ExecutionSession session) {
        session.stepLock.lock();
        try {
            session.waitingForStep = false;
            session.stepCondition.signalAll();
        } finally {
            session.stepLock.unlock();
        }
    }

    private Map<Long, Long> buildFallbackConnections(Map<Long, Block> blockMap) {
        Map<Long, Long> fallbackMap = new HashMap<>();

        for (Block block : blockMap.values()) {
            if (block instanceof ControlBlock) {
                ControlBlock controlBlock = (ControlBlock) block;
                registerBranchFallback(controlBlock, controlBlock.getTrueBranchId(), blockMap, fallbackMap);
                registerBranchFallback(controlBlock, controlBlock.getFalseBranchId(), blockMap, fallbackMap);
            }
        }

        return fallbackMap;
    }

    private void registerBranchFallback(ControlBlock parent, Long branchStartId,
                                        Map<Long, Block> blockMap, Map<Long, Long> fallbackMap) {
        if (branchStartId == null) {
            return;
        }

        Set<Long> visited = new HashSet<>();
        Long current = branchStartId;

        while (current != null && visited.add(current)) {
            Block branchBlock = blockMap.get(current);
            if (branchBlock == null) {
                break;
            }
            // 분기 내부 블록이 부모(Control) 자신을 가리키면 더 이상 fallback을 계산하지 않는다.
            // (이미 명시적으로 부모로 돌아가므로 부모에 대한 fallback을 만들면 false 분기에서도 무한 루프가 생길 수 있다)
            if (branchBlock.getId() != null && branchBlock.getId().equals(parent.getId())) {
                break;
            }

            Long explicitNext = branchBlock.getNextBlockId();
            if (explicitNext == null) {
                Long fallbackNext;
                if (parent instanceof WhileBlock || parent instanceof ForBlock) {
                    fallbackNext = parent.getId();
                } else {
                    fallbackNext = parent.getNextBlockId();
                }

                fallbackMap.putIfAbsent(current, fallbackNext);
                break;
            }

            current = explicitNext;
        }
    }

    private void preloadExpressionBlocks(List<Block> blocks) {
        Set<Long> visited = new HashSet<>();

        for (Block block : blocks) {
            if (block instanceof ControlBlock control) {
                initializeExpression(control.getConditionExpressionBlock(), visited);
                if (block instanceof ForBlock forBlock) {
                    initializeExpression(forBlock.getInitExpressionBlock(), visited);
                    initializeExpression(forBlock.getIncrementExpressionBlock(), visited);
                }
            }

            if (block instanceof ArithmeticBlock arithmetic) {
                initializeExpression(arithmetic.getOperand1Block(), visited);
                initializeExpression(arithmetic.getOperand2Block(), visited);
            } else if (block instanceof VariableDeclareBlock declareBlock) {
                initializeExpression(declareBlock.getInitialExpressionBlock(), visited);
            } else if (block instanceof VariableAssignBlock assignBlock) {
                initializeExpression(assignBlock.getValueExpressionBlock(), visited);
            } else if (block instanceof team8.model.output.PrintBlock printBlock) {
                initializeExpression(printBlock.getMessageExpressionBlock(), visited);
            }
        }
    }

    private void initializeExpression(ExpressionBlock expressionBlock, Set<Long> visited) {
        if (expressionBlock == null) {
            return;
        }
        Hibernate.initialize(expressionBlock);
        Long id = expressionBlock.getId();
        if (id != null && !visited.add(id)) {
            return;
        }

        if (expressionBlock instanceof UnaryExpressionBlock unary) {
            initializeExpression(unary.getOperand(), visited);
        } else if (expressionBlock instanceof BinaryExpressionBlock binary) {
            initializeExpression(binary.getLeft(), visited);
            initializeExpression(binary.getRight(), visited);
        }
    }

    private static class ExecutionSession {
        private final String sessionId;
        private final ExecutionContext context;
        private final Map<Long, Block> blockMap;
        private final Map<Long, Long> fallbackNextMap;
        private final Lock stepLock = new ReentrantLock();
        private final Condition stepCondition = stepLock.newCondition();
        private volatile boolean waitingForStep = false;
        private volatile Thread runnerThread;

        private ExecutionSession(String sessionId, ExecutionContext context,
                                 Map<Long, Block> blockMap, Map<Long, Long> fallbackNextMap) {
            this.sessionId = sessionId;
            this.context = context;
            this.blockMap = blockMap;
            this.fallbackNextMap = fallbackNextMap;
        }
    }

    public enum StepResult {
        STEPPED,
        NOT_FOUND,
        NOT_IN_DEBUG_MODE
    }
}
