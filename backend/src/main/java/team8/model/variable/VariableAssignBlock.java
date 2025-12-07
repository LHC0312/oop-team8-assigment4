package team8.model.variable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;
import team8.model.Block;
import team8.model.expression.ExpressionBlock;

@Entity
@Table(name = "variable_assign_blocks")
@DiscriminatorValue("VAR_ASSIGN")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class VariableAssignBlock extends VariableBlock {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "value_expression_id")
    private ExpressionBlock valueExpressionBlock;

    @Override
    public String getBlockType() {
        return "VAR_ASSIGN";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (getVariableId() == null) {
            throw new IllegalStateException("variableId is required for VAR_ASSIGN");
        }
        Object value = context.evaluateExpression(valueExpressionBlock);
        if (value == null) {
            Object existing = context.getVariable(getVariableId());
            value = existing != null ? existing : "";
        }
        String type = context.getVariableType(getVariableId());
        if (type == null) {
            String identifier = getVariableName() != null ? getVariableName() : String.valueOf(getVariableId());
            throw new IllegalStateException("Variable type not declared for: " + identifier);
        }
        Object coerced = coerceToType(value, type);
        context.setVariable(getVariableId(), coerced);
        context.sendDebug("VAR_ASSIGN", getVariableName() + " = " + coerced + " (" + type + ")");
        return new ExecutionResult(getNextBlockId());
    }

    private Object coerceToType(Object value, String type) {
        if ("boolean".equalsIgnoreCase(type)) {
            if (value instanceof Boolean) {
                return value;
            }
            if (value == null) {
                return false;
            }
            String text = value.toString().toLowerCase();
            if ("true".equals(text)) return true;
            if ("false".equals(text)) return false;
            throw new IllegalArgumentException("Cannot convert to boolean: " + value);
        }
        if ("number".equalsIgnoreCase(type)) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value == null) {
                return 0.0;
            }
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert to number: " + value);
            }
        }
        if ("string".equalsIgnoreCase(type)) {
            return value == null ? "" : value.toString();
        }
        throw new IllegalArgumentException("Unsupported variableType: " + type);
    }
}
