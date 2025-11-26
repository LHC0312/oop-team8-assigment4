package team8.model.variable;

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
@Table(name = "variable_declare_blocks")
@DiscriminatorValue("VAR_DECLARE")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class VariableDeclareBlock extends Block {

    @Column(name = "variable_name")
    private String variableName;

    @Column(name = "variable_type")
    private String variableType;

    @Column(name = "initial_value")
    private String initialValue;

    @Override
    public String getBlockType() {
        return "VAR_DECLARE";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object value = context.evaluateExpression(initialValue);
        context.setVariable(variableName, value);
        context.sendOutput("VAR_DECLARE", variableName + " = " + value);
        return new ExecutionResult(getNextBlockId());
    }
}
