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

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "draw_blocks")
@DiscriminatorValue("DRAW")
@Data
@EqualsAndHashCode(callSuper = true)


@SuperBuilder
@NoArgsConstructor
public class DrawBlock extends OutputBlock {

    @Column(name = "shape")
    private String shape;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private Integer size;

    @Override
    public String getBlockType() {
        return "DRAW";
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Map<String, Object> drawData = new HashMap<>();
        drawData.put("shape", shape);
        drawData.put("color", color);
        drawData.put("size", size);
        context.sendOutput("DRAW", drawData);
        return new ExecutionResult(getNextBlockId());
    }

    @Override
    public Object getOutputContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("shape", shape);
        content.put("color", color);
        content.put("size", size);
        return content;
    }
}
