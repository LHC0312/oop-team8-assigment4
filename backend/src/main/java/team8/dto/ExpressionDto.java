package team8.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionDto {
    private Long id;
    private String type;
    private String operator;
    private String value;
    private String variableName;
    private Long variableId;
    private Long leftId;
    private Long rightId;
    private Long operandId;
}
