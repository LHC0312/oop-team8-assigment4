package team8.model.output;

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
@Table(name = "alert_blocks")
@DiscriminatorValue("ALERT")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class AlertBlock extends OutputBlock {

    @Column(name = "message", length = 1000)
    private String message;

    @Override
    public String getBlockType() {
        return "ALERT";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object evaluatedMessage = context.evaluateExpression(message);
        context.sendOutput("ALERT", evaluatedMessage);
        return new ExecutionResult(getNextBlockId());
    }

    @Override
    public Object getOutputContent() {
        return message;
    }
}
