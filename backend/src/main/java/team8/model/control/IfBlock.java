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
@Table(name = "if_blocks")
@DiscriminatorValue("IF")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class IfBlock extends ControlBlock {

    @Override
    public String getBlockType() {
        return "IF";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        boolean condition = context.evaluateCondition(getConditionExpression());
        context.sendOutput("IF", "Condition: " + getConditionExpression() + " = " + condition);

        if (condition) {
            return new ExecutionResult(getTrueBranchId());
        } else {
            return new ExecutionResult(getFalseBranchId() != null ? getFalseBranchId() : getNextBlockId());
        }
    }
}
