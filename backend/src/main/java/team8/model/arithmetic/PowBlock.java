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
@Table(name = "pow_blocks")
@DiscriminatorValue("POW")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class PowBlock extends Block {

    @Column(name = "operand1")
    private String operand1;

    @Column(name = "operand2")
    private String operand2;

    @Column(name = "result_variable")
    private String resultVariable;

    @Override
    public String getBlockType() {
        return "POW";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object val1 = context.evaluateExpression(operand1);
        Object val2 = context.evaluateExpression(operand2);

        double num1 = val1 instanceof Number ? ((Number) val1).doubleValue() : Double.parseDouble(val1.toString());
        double num2 = val2 instanceof Number ? ((Number) val2).doubleValue() : Double.parseDouble(val2.toString());

        // 거듭제곱
        double result = Math.pow(num1, num2);

        context.setVariable(resultVariable, result);
        context.sendOutput("POW", resultVariable + " = " + num1 + " ^ " + num2 + " = " + result);

        return new ExecutionResult(getNextBlockId());
    }
}
