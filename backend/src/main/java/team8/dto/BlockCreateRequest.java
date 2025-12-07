package team8.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import team8.dto.ValueDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockCreateRequest {
    private String blockType;
    private Integer positionX;
    private Integer positionY;
    private Integer order;
    private Long nextBlockId;

    private Long trueBranchId;
    private Long falseBranchId;

    private ValueDto condition;
    private ValueDto init;
    private ValueDto increment;
    private ValueDto message;

    private String variableName; // VAR_DECLARE 전용
    private String variableType;
    private Long variableId; // 참조용 (VAR_ASSIGN 등)
    private ValueDto initial;
    private ValueDto value;
}
