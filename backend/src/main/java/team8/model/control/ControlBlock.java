package team8.model.control;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.model.Block;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public abstract class ControlBlock extends Block {

    @Column(name = "condition_expression", length = 500)
    private String conditionExpression;

    @Column(name = "true_branch_id")
    private Long trueBranchId;

    @Column(name = "false_branch_id")
    private Long falseBranchId;
}
