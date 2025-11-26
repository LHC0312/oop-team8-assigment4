package team8.model.control;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;

@Entity
@Table(name = "for_blocks")
@DiscriminatorValue("FOR")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ForBlock extends ControlBlock {

    @Column(name = "init_expression")
    private String initExpression;

    @Column(name = "increment_expression")
    private String incrementExpression;

    @Override
    public String getBlockType() {
        return "FOR";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        boolean condition = context.evaluateCondition(getConditionExpression());
        context.sendOutput("FOR", "Condition: " + getConditionExpression() + " = " + condition);

        if (condition) {
            return new ExecutionResult(getTrueBranchId());
        } else {
            return new ExecutionResult(getNextBlockId());
        }
    }
}
