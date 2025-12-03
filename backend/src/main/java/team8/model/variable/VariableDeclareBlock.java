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
@Table(name = "variable_declare_blocks")
@DiscriminatorValue("VAR_DECLARE")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class VariableDeclareBlock extends VariableBlock {

    @Column(name = "variable_type")
    private String variableType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "initial_expression_id")
    private ExpressionBlock initialExpressionBlock;

    @Override
    public String getBlockType() {
        return "VAR_DECLARE";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (getVariableId() == null) {
            throw new IllegalStateException("variableId is required for VAR_DECLARE");
        }
        Object value = context.evaluateExpression(initialExpressionBlock);
        if (value == null) {
            value = defaultValueByType();
        }
        Object typed = coerceToType(value, variableType);
        context.setVariableMeta(getVariableId(), getVariableName(), variableType);
        context.setVariable(getVariableId(), typed);
        context.sendDebug("VAR_DECLARE", getVariableName() + " = " + typed + " (" + variableType + ")");
        return new ExecutionResult(getNextBlockId());
    }

    private Object defaultValueByType() {
        if ("boolean".equalsIgnoreCase(variableType)) {
            return false;
        }
        if ("number".equalsIgnoreCase(variableType)) {
            return 0.0;
        }
        return "";
    }

    private Object coerceToType(Object value, String type) {
        if (type == null) {
            throw new IllegalArgumentException("variableType is required");
        }
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
