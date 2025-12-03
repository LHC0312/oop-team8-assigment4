package team8.model.output;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;
import team8.model.Block;
import team8.model.expression.ExpressionBlock;

@Entity
@Table(name = "print_blocks")
@DiscriminatorValue("PRINT")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class PrintBlock extends OutputBlock {

    @ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "message_expression_id")
    private ExpressionBlock messageExpressionBlock;

    @Override
    public String getBlockType() {
        return "PRINT";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object evaluatedMessage = null;
        if (messageExpressionBlock != null) {
            evaluatedMessage = context.evaluateExpression(messageExpressionBlock);
        }

        if (evaluatedMessage == null) {
            evaluatedMessage = previewExpression(messageExpressionBlock);
        }

        context.sendOutput("PRINT", evaluatedMessage == null ? "" : evaluatedMessage);
        return new ExecutionResult(getNextBlockId());
    }

    @Override
    public Object getOutputContent() {
        return messageExpressionBlock != null ? contextSafeValue(messageExpressionBlock) : null;
    }

    private Object contextSafeValue(ExpressionBlock block) {
        // 메시지 미리보기 용도. 실행 컨텍스트가 없어도 대략적인 값을 주기 위해 null 반환 가능.
        return null;
    }

    private Object previewExpression(ExpressionBlock block) {
        if (block == null) {
            return null;
        }
        if (block instanceof team8.model.expression.LiteralExpressionBlock literal) {
            return literal.getValue();
        }
        if (block instanceof team8.model.expression.VariableExpressionBlock variable) {
            return variable.getVariableName();
        }
        return null;
    }
}
