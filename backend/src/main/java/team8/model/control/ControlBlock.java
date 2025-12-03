package team8.model.control;

import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.CascadeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.model.Block;
import team8.model.expression.ExpressionBlock;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public abstract class ControlBlock extends Block {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "condition_expression_id")
    private ExpressionBlock conditionExpressionBlock;

    @Column(name = "true_branch_id")
    private Long trueBranchId;

    @Column(name = "false_branch_id")
    private Long falseBranchId;
}
