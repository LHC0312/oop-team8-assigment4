package team8.dto;

import lombok.Data;

@Data
public class ExpressionCreateRequest {
    private String type;         // LITERAL, VARIABLE, UNARY, BINARY
    private String operator;     // for UNARY/BINARY
    private String value;        // for LITERAL
    private String variableName; // for VARIABLE
    private Long variableId;     // for VARIABLE
    private Long leftId;         // for BINARY
    private Long rightId;        // for BINARY
    private Long operandId;      // for UNARY
}
