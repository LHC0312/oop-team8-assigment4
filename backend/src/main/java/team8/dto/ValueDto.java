package team8.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 값/표현식 블록을 표현하는 DTO.
 * blockId는 DB에 저장된 표현식 블록 ID를 그대로 노출한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueDto {
    /**
     * 표현식 블록 ID (데이터베이스의 ID)
     */
    private Long blockId;

    /**
     * 표현식 노드의 캔버스 위치 (옵션)
     */
    private Integer positionX;
    private Integer positionY;

    /**
     * LITERAL, VARIABLE, UNARY, BINARY
     */
    private String valueType;

    /**
     * LITERAL 값 (string/number/boolean)
     */
    private Object data;

    /**
     * VARIABLE 이름
     */
    private String variableName;
    /**
     * VARIABLE ID (참조 무결성 보장)
     */
    private Long variableId;

    /**
     * UNARY/BINARY 연산자
     */
    private String operator;

    /**
     * UNARY 피연산자 (새 표현식 생성 시 사용)
     */
    private ValueDto operand;
    /**
     * UNARY 피연산자 ID (응답 시 사용)
     */
    private Long operandId;

    /**
     * BINARY 좌/우 피연산자 (새 표현식 생성 시 사용)
     */
    private ValueDto left;
    private ValueDto right;
    /**
     * BINARY 좌/우 피연산자 ID (응답 시 사용)
     */
    private Long leftId;
    private Long rightId;
}
