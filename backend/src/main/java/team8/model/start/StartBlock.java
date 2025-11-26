package team8.model.start;

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
@Table(name = "start_blocks")
@DiscriminatorValue("START")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class StartBlock extends Block {

    @Override
    public String getBlockType() {
        return "START";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        context.sendOutput("START", "Program started");
        return new ExecutionResult(getNextBlockId());
    }
}
