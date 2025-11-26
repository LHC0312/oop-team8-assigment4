package team8.model.output;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.model.Block;

/**
 * 출력 블록 추상 클래스
 * PRINT, ALERT, DRAW 등 화면에 출력하는 블록의 공통 부모
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public abstract class OutputBlock extends Block {

    /**
     * 출력할 내용을 반환합니다.
     * @return 출력 내용
     */
    public abstract Object getOutputContent();
}
