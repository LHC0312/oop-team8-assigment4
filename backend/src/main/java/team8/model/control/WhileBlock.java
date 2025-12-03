package team8.model.control;

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
@Table(name = "while_blocks")
@DiscriminatorValue("WHILE")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class WhileBlock extends ControlBlock {

    @Override
    public String getBlockType() {
        return "WHILE";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        boolean condition = context.evaluateCondition(getConditionExpressionBlock());
        context.sendDebug("WHILE", "Condition: " + condition);

        if (condition) {
            return new ExecutionResult(getTrueBranchId());
        } else {
            return new ExecutionResult(getNextBlockId());
        }
    }
}
