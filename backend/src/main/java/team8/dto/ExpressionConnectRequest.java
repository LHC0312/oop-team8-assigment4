package team8.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 표현식(값) 블록 간 연결(자식 표현식 ID) 갱신용 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionConnectRequest {
    private Long expressionId;
    private Long operandExpressionId;
    private Long leftExpressionId;
    private Long rightExpressionId;
}
