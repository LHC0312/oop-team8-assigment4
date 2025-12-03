package team8.model.arithmetic;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.model.Block;
import team8.model.expression.ExpressionBlock;

/**
 * 산술 연산 블록 추상 클래스
 * ADD, SUBTRACT, MULTIPLY, DIVIDE 등 사칙연산 블록의 공통 부모
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public abstract class ArithmeticBlock extends Block {

    @ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "operand1_expr_id")
    private ExpressionBlock operand1Block;

    @ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "operand2_expr_id")
    private ExpressionBlock operand2Block;

    @Column(name = "result_variable")
    private String resultVariable;

    @Column(name = "result_variable_id")
    private Long resultVariableId;
}
