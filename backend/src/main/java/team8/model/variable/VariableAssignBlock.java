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
@Table(name = "variable_assign_blocks")
@DiscriminatorValue("VAR_ASSIGN")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class VariableAssignBlock extends Block {

    @Column(name = "variable_name")
    private String variableName;

    @Column(name = "value_expression")
    private String valueExpression;

    @Override
    public String getBlockType() {
        return "VAR_ASSIGN";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object value = context.evaluateExpression(valueExpression);
        context.setVariable(variableName, value);
        context.sendOutput("VAR_ASSIGN", variableName + " = " + value);
        return new ExecutionResult(getNextBlockId());
    }
}
