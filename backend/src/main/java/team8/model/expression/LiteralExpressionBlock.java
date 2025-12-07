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
@Table(name = "literal_expression_blocks")
@DiscriminatorValue("LITERAL")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class LiteralExpressionBlock extends ExpressionBlock {

    @Column(name = "value", length = 1000)
    private String value;

    @Column(name = "literal_type", length = 20)
    private String literalType;

    public LiteralExpressionBlock(String value) {
        super(null, null, null, null);
        this.value = value;
    }

    public LiteralExpressionBlock(String value, String literalType) {
        super(null, null, null, null);
        this.value = value;
        this.literalType = literalType;
    }

    @Override
    public Object evaluate(ExecutionContext context) {
        return context.parseLiteral(this);
    }

    @Override
    public String evaluateAsString(ExecutionContext context) {
        return value == null ? "" : value;
    }

    @Override
    public String getExpressionType() {
        return "LITERAL";
    }
}
