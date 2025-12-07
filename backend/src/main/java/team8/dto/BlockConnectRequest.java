package team8.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 블록 간 연결 정보를 일괄 갱신하기 위한 요청 DTO.
 * 필요 항목만 채우면 해당 필드만 갱신된다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockConnectRequest {
    private Long blockId;
    private Long nextBlockId;
    private Long trueBranchId;
    private Long falseBranchId;
}
