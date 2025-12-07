package team8.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 표현식(값) 블록 간 연결(자식 표현식 ID) 갱신용 DTO.
 */
@Getter
@NoArgsConstructor
public class ExpressionConnectRequest {
    private Long expressionId;
    private Long operandExpressionId;
    private Long leftExpressionId;
    private Long rightExpressionId;
    
    // 명시적으로 필드가 포함되었는지 추적
    private boolean hasOperandExpressionId;
    private boolean hasLeftExpressionId;
    private boolean hasRightExpressionId;
    
    public void setExpressionId(Long expressionId) {
        this.expressionId = expressionId;
    }
    
    public void setOperandExpressionId(Long operandExpressionId) {
        this.operandExpressionId = operandExpressionId;
        this.hasOperandExpressionId = true;
    }
    
    public void setLeftExpressionId(Long leftExpressionId) {
        this.leftExpressionId = leftExpressionId;
        this.hasLeftExpressionId = true;
    }
    
    public void setRightExpressionId(Long rightExpressionId) {
        this.rightExpressionId = rightExpressionId;
        this.hasRightExpressionId = true;
    }
}
