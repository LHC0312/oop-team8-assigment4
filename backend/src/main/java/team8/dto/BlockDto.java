package team8.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockDto {
    private Long id;
    private String blockType;
    private Integer positionX;
    private Integer positionY;
    private Integer order;
    private Long nextBlockId;

    private Long trueBranchId;
    private Long falseBranchId;

    // Expression fields - ID only (full data from /api/expressions)
    private Long conditionExpressionId;
    private Long initExpressionId;
    private Long incrementExpressionId;
    private Long messageExpressionId;
    private Long initialExpressionId;
    private Long valueExpressionId;

    private String variableName;
    private String variableType;
    private Long variableId;
}
