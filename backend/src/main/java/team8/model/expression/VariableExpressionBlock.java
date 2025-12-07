package team8.model.expression;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;

@Entity
@Table(name = "variable_expression_blocks")
@DiscriminatorValue("VARIABLE")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class VariableExpressionBlock extends ExpressionBlock {

    @Column(name = "variable_name")
    private String variableName;

    @Column(name = "variable_id")
    private Long variableId;

    public VariableExpressionBlock(String variableName) {
        super(null, null, null, null);
        this.variableName = variableName;
    }

    public VariableExpressionBlock(Long variableId, String variableName) {
        super(null, null, null, null);
        this.variableId = variableId;
        this.variableName = variableName;
    }

    @Override
    public Object evaluate(ExecutionContext context) {
        return context.resolveVariableValue(this);
    }

    @Override
    public String getExpressionType() {
        return "VARIABLE";
    }
}
