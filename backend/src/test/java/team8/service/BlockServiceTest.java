package team8.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team8.dto.BlockCreateRequest;
import team8.dto.BlockDto;
import team8.model.Block;
import team8.model.Project;
import team8.model.output.PrintBlock;
import team8.model.start.StartBlock;
import team8.repository.BlockRepository;
import team8.repository.ProjectRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private BlockService blockService;

    private Project testProject;
    private Block testBlock;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .build();

        testBlock = StartBlock.builder()
                .id(1L)
                .positionX(100)
                .positionY(200)
                .order(1)
                .nextBlockId(null)
                .project(testProject)
                .build();
    }

    @Test
    @DisplayName("프로젝트의 블록 목록 조회 테스트")
    void testGetBlocksByProjectId() {
        when(blockRepository.findByProjectIdOrderByOrderAsc(1L)).thenReturn(Arrays.asList(testBlock));

        List<BlockDto> blocks = blockService.getBlocksByProjectId(1L);

        assertNotNull(blocks);
        assertEquals(1, blocks.size());
        assertEquals("START", blocks.get(0).getBlockType());

        verify(blockRepository, times(1)).findByProjectIdOrderByOrderAsc(1L);
    }

    @Test
    @DisplayName("블록 ID로 조회 테스트")
    void testGetBlockById() {
        when(blockRepository.findById(1L)).thenReturn(Optional.of(testBlock));

        BlockDto block = blockService.getBlockById(1L);

        assertNotNull(block);
        assertEquals(1L, block.getId());
        assertEquals("START", block.getBlockType());

        verify(blockRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("START 블록 생성 테스트")
    void testCreateStartBlock() {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("START");
        request.setPositionX(100);
        request.setPositionY(200);
        request.setOrder(1);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(blockRepository.save(any(Block.class))).thenReturn(testBlock);

        BlockDto created = blockService.createBlock(1L, request);

        assertNotNull(created);
        assertEquals("START", created.getBlockType());

        verify(projectRepository, times(1)).findById(1L);
        verify(blockRepository, times(1)).save(any(Block.class));
    }

    @Test
    @DisplayName("PRINT 블록 생성 테스트")
    void testCreatePrintBlock() {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("PRINT");
        request.setMessage("Hello World");
        request.setPositionX(100);
        request.setPositionY(200);

        PrintBlock printBlock = PrintBlock.builder()
                .id(2L)
                .message("Hello World")
                .positionX(100)
                .positionY(200)
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(blockRepository.save(any(Block.class))).thenReturn(printBlock);

        BlockDto created = blockService.createBlock(1L, request);

        assertNotNull(created);
        assertEquals("PRINT", created.getBlockType());
        assertEquals("Hello World", created.getMessage());

        verify(projectRepository, times(1)).findById(1L);
        verify(blockRepository, times(1)).save(any(Block.class));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트에 블록 생성 시 예외 발생")
    void testCreateBlockWithNonExistentProject() {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("START");

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            blockService.createBlock(999L, request);
        });

        verify(projectRepository, times(1)).findById(999L);
        verify(blockRepository, never()).save(any(Block.class));
    }

    @Test
    @DisplayName("블록 삭제 테스트")
    void testDeleteBlock() {
        doNothing().when(blockRepository).deleteById(1L);

        blockService.deleteBlock(1L);

        verify(blockRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("알 수 없는 블록 타입 생성 시 예외 발생")
    void testCreateBlockWithUnknownType() {
        BlockCreateRequest request = new BlockCreateRequest();
        request.setBlockType("UNKNOWN_TYPE");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        assertThrows(RuntimeException.class, () -> {
            blockService.createBlock(1L, request);
        });

        verify(projectRepository, times(1)).findById(1L);
    }
}
