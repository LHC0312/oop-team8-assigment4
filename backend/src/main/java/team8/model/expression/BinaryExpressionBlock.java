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
@Table(name = "binary_expression_blocks")
@DiscriminatorValue("BINARY")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class BinaryExpressionBlock extends ExpressionBlock {

    @Column(name = "operator", length = 10)
    private String operator;

    @Column(name = "left_expr_id")
    private Long leftExpressionId;

    @Column(name = "right_expr_id")
    private Long rightExpressionId;

    @Transient
    private ExpressionBlock left;

    @Transient
    private ExpressionBlock right;

    public BinaryExpressionBlock(String operator, ExpressionBlock left, ExpressionBlock right) {
        super(null, null, null, null);
        this.operator = operator;
        setLeft(left);
        setRight(right);
    }

    public BinaryExpressionBlock(String operator, Long leftExpressionId, Long rightExpressionId) {
        super(null, null, null, null);
        this.operator = operator;
        this.leftExpressionId = leftExpressionId;
        this.rightExpressionId = rightExpressionId;
    }

    public void setLeft(ExpressionBlock left) {
        this.left = left;
        this.leftExpressionId = left == null ? null : left.getId();
    }

    public void setRight(ExpressionBlock right) {
        this.right = right;
        this.rightExpressionId = right == null ? null : right.getId();
    }

    @Override
    public Object evaluate(ExecutionContext context) {
        ExpressionBlock leftBlock = resolveLeft(context);
        ExpressionBlock rightBlock = resolveRight(context);
        Object leftValue = context.evaluateExpression(leftBlock);
        Object rightValue = context.evaluateExpression(rightBlock);
        return context.applyBinary(operator, leftValue, rightValue);
    }

    @Override
    public String evaluateAsString(ExecutionContext context) {
        if ("+".equals(operator)) {
            String leftStr = context.evaluateExpressionAsString(resolveLeft(context));
            String rightStr = context.evaluateExpressionAsString(resolveRight(context));
            return leftStr + rightStr;
        }
        Object result = evaluate(context);
        return result == null ? "" : result.toString();
    }

    @Override
    public String getExpressionType() {
        return "BINARY";
    }

    private ExpressionBlock resolveLeft(ExecutionContext context) {
        if (left != null) {
            return left;
        }
        ExpressionBlock resolved = context.loadExpressionById(leftExpressionId);
        setLeft(resolved);
        return resolved;
    }

    private ExpressionBlock resolveRight(ExecutionContext context) {
        if (right != null) {
            return right;
        }
        ExpressionBlock resolved = context.loadExpressionById(rightExpressionId);
        setRight(resolved);
        return resolved;
    }
}
