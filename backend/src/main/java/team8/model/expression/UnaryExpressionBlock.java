package team8.model.expression;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;

@Entity
@Table(name = "unary_expression_blocks")
@DiscriminatorValue("UNARY")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UnaryExpressionBlock extends ExpressionBlock {

    @Column(name = "operator", length = 5)
    private String operator;

    @Column(name = "operand_expr_id")
    private Long operandExpressionId;

    @Transient
    private ExpressionBlock operand;

    public UnaryExpressionBlock(String operator, ExpressionBlock operand) {
        super(null, null, null, null);
        this.operator = operator;
        setOperand(operand);
    }

    public UnaryExpressionBlock(String operator, Long operandExpressionId) {
        super(null, null, null, null);
        this.operator = operator;
        this.operandExpressionId = operandExpressionId;
    }

    public void setOperand(ExpressionBlock operand) {
        this.operand = operand;
        this.operandExpressionId = operand == null ? null : operand.getId();
    }

    @Override
    public Object evaluate(ExecutionContext context) {
        ExpressionBlock operandBlock = resolveOperand(context);
        Object operandValue = context.evaluateExpression(operandBlock);
        return context.applyUnary(operator, operandValue);
    }

    @Override
    public String evaluateAsString(ExecutionContext context) {
        ExpressionBlock operandBlock = resolveOperand(context);
        Object operandValue = context.evaluateExpression(operandBlock);
        if (operandValue == null) {
            return "";
        }
        Object result = context.applyUnary(operator, operandValue);
        return result == null ? "" : result.toString();
    }

    @Override
    public String getExpressionType() {
        return "UNARY";
    }

    private ExpressionBlock resolveOperand(ExecutionContext context) {
        if (operand != null) {
            return operand;
        }
        ExpressionBlock resolved = context.loadExpressionById(operandExpressionId);
        setOperand(resolved);
        return resolved;
    }
}
