package team8.model.arithmetic;

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
import team8.model.Block;

@Entity
@Table(name = "add_blocks")
@DiscriminatorValue("ADD")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class AddBlock extends ArithmeticBlock {

    @Override
    public String getBlockType() {
        return "ADD";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object val1 = context.evaluateExpression(getOperand1Block());
        Object val2 = context.evaluateExpression(getOperand2Block());

        if (getResultVariableId() == null) {
            throw new IllegalStateException("resultVariableId is required for ADD block");
        }

        double num1 = (val1 instanceof Number) ? ((Number) val1).doubleValue() : Double.parseDouble(String.valueOf(val1));
        double num2 = (val2 instanceof Number) ? ((Number) val2).doubleValue() : Double.parseDouble(String.valueOf(val2));
        double result = num1 + num2;

        context.setVariable(getResultVariableId(), result);
        context.sendDebug("ADD", getResultVariable() + " = " + num1 + " + " + num2 + " = " + result);
        return new ExecutionResult(getNextBlockId());
    }
}
