package team8.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team8.dto.ValueDto;

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

    private ValueDto condition;
    private ValueDto init;
    private ValueDto increment;

    private ValueDto message;

    private ValueDto operand1;
    private ValueDto operand2;
    private String resultVariable;
    private Long resultVariableId;

    private String variableName;
    private String variableType;
    private Long variableId;
    private ValueDto initial;
    private ValueDto value;
}
