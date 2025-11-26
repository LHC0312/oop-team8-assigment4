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

    private String conditionExpression;
    private Long trueBranchId;
    private Long falseBranchId;

    private String initExpression;
    private String incrementExpression;

    private String message;

    private String shape;
    private String color;
    private Integer size;

    private String operand1;
    private String operand2;
    private String resultVariable;

    private String variableName;
    private String variableType;
    private String initialValue;
    private String valueExpression;
}
