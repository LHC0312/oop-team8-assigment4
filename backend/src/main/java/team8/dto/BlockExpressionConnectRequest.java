package team8.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 블록의 표현식 슬롯 연결/해제 요청 DTO.
 * 슬롯 이름(slot)과 표현식 ID(expressionId)를 지정한다.
 * expressionId가 null이면 해당 슬롯의 표현식 연결을 해제한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockExpressionConnectRequest {
    /** 연결을 변경할 블록 ID */
    private Long blockId;
    
    /** 
     * 슬롯 이름 (condition, message, value, initial, init, increment)
     * - condition: IF, WHILE, FOR 블록의 조건
     * - message: PRINT 블록의 메시지
     * - value: VAR_ASSIGN 블록의 값
     * - initial: VAR_DECLARE 블록의 초기값
     * - init: FOR 블록의 초기화
     * - increment: FOR 블록의 증감
     */
    private String slot;
    
    /** 연결할 표현식 ID (null이면 연결 해제) */
    private Long expressionId;
}
