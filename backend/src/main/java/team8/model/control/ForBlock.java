package team8.model.control;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;
import team8.model.expression.ExpressionBlock;

@Entity
@Table(name = "for_blocks")
@DiscriminatorValue("FOR")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ForBlock extends ControlBlock {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "init_expression_id")
    private ExpressionBlock initExpressionBlock;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "increment_expression_id")
    private ExpressionBlock incrementExpressionBlock;

    @Override
    public String getBlockType() {
        return "FOR";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        boolean condition = context.evaluateCondition(getConditionExpressionBlock());
        context.sendDebug("FOR", "Condition: " + condition);

        if (condition) {
            return new ExecutionResult(getTrueBranchId());
        } else {
            return new ExecutionResult(getNextBlockId());
        }
    }
}
