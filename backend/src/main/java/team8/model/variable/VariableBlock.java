package team8.model.variable;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.model.Block;

/**
 * 변수 블록 추상 클래스
 * VAR_DECLARE, VAR_ASSIGN 등 변수를 다루는 블록의 공통 부모
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public abstract class VariableBlock extends Block {

    @Column(name = "variable_name")
    private String variableName;

    @Column(name = "variable_id")
    private Long variableId;
}
