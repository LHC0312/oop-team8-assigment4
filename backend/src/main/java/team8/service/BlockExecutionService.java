package team8.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;
import team8.model.Block;
import team8.model.Project;
import team8.model.start.StartBlock;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BlockExecutionService {

    private final BlockRepository blockRepository;
    private final ProjectRepository projectRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public void executeProject(Long projectId, String sessionId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Block> blocks = blockRepository.findByProjectIdOrderByOrderAsc(projectId);

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

        ExecutionContext context = new ExecutionContext(messagingTemplate, sessionId);

        try {
            executeFromBlock(startBlock, blockMap, context);
            context.sendOutput("COMPLETE", "Execution completed successfully");
        } catch (Exception e) {
            context.sendOutput("ERROR", "Execution error: " + e.getMessage());
        }
    }

    private void executeFromBlock(Block currentBlock, Map<Long, Block> blockMap, ExecutionContext context) {
        int maxIterations = 10000;
        int iterations = 0;

        while (currentBlock != null && !context.isStopped()) {
            if (iterations++ > maxIterations) {
                throw new RuntimeException("Maximum iteration limit reached (possible infinite loop)");
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            ExecutionResult result = currentBlock.execute(context);

            if (result.getNextBlockId() == null) {
                break;
            }

            currentBlock = blockMap.get(result.getNextBlockId());
        }
    }
}
