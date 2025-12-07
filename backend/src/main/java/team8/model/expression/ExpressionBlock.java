package team8.model.expression;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;
import team8.model.Project;

@Entity
@Table(name = "expression_blocks")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "expression_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ExpressionBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    public abstract String getExpressionType();

    public abstract Object evaluate(ExecutionContext context);

    /**
     * 문자열 평가 결과를 반환한다.
     */
    public String evaluateAsString(ExecutionContext context) {
        Object value = evaluate(context);
        return value == null ? "" : value.toString();
    }

    /**
     * 출력 등에서 일관된 실행 함수를 제공하기 위해 추가.
     * 문자열 평가 결과를 반환한다.
     */
    public String execute(ExecutionContext context) {
        return evaluateAsString(context);
    }
}
