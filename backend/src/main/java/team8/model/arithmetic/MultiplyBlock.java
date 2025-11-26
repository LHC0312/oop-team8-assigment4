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
@Table(name = "multiply_blocks")
@DiscriminatorValue("MULTIPLY")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class MultiplyBlock extends ArithmeticBlock {

    @Override
    public String getBlockType() {
        return "MULTIPLY";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object val1 = context.evaluateExpression(getOperand1());
        Object val2 = context.evaluateExpression(getOperand2());

        double num1 = val1 instanceof Number ? ((Number) val1).doubleValue() : Double.parseDouble(val1.toString());
        double num2 = val2 instanceof Number ? ((Number) val2).doubleValue() : Double.parseDouble(val2.toString());
        double result = num1 * num2;

        context.setVariable(getResultVariable(), result);
        context.sendOutput("MULTIPLY", getResultVariable() + " = " + num1 + " * " + num2 + " = " + result);
        return new ExecutionResult(getNextBlockId());
    }
}
