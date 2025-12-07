package team8.service;

import org.hibernate.proxy.HibernateProxy;
import team8.dto.ValueDto;
import team8.model.expression.BinaryExpressionBlock;
import team8.model.expression.ExpressionBlock;
import team8.model.expression.LiteralExpressionBlock;
import team8.model.expression.UnaryExpressionBlock;
import team8.model.expression.VariableExpressionBlock;

import java.util.function.Function;

public final class BlockServiceHelper {

    private BlockServiceHelper() {}

    public static ValueDto toValueDto(ExpressionBlock block, Function<Long, ExpressionBlock> expressionLoader) {
        if (block == null) {
            return null;
        }

        if (block instanceof HibernateProxy proxy) {
            block = (ExpressionBlock) proxy.getHibernateLazyInitializer().getImplementation();
        }

        if (block instanceof LiteralExpressionBlock literal) {
            return ValueDto.builder()
                    .blockId(literal.getId())
                    .positionX(literal.getPositionX())
                    .positionY(literal.getPositionY())
                    .valueType("LITERAL")
                    .data(literal.getValue())
                    .build();
        }
        if (block instanceof VariableExpressionBlock variable) {
            return ValueDto.builder()
                    .blockId(variable.getId())
                    .positionX(variable.getPositionX())
                    .positionY(variable.getPositionY())
                    .valueType("VARIABLE")
                    .variableName(variable.getVariableName())
                    .variableId(variable.getVariableId())
                    .build();
        }
        if (block instanceof UnaryExpressionBlock unary) {
            return ValueDto.builder()
                    .blockId(unary.getId())
                    .positionX(unary.getPositionX())
                    .positionY(unary.getPositionY())
                    .valueType("UNARY")
                    .operator(unary.getOperator())
                    .operandId(unary.getOperandExpressionId())
                    .build();
        }
        if (block instanceof BinaryExpressionBlock binary) {
            return ValueDto.builder()
                    .blockId(binary.getId())
                    .positionX(binary.getPositionX())
                    .positionY(binary.getPositionY())
                    .valueType("BINARY")
                    .operator(binary.getOperator())
                    .leftId(binary.getLeftExpressionId())
                    .rightId(binary.getRightExpressionId())
                    .build();
        }
        throw new IllegalArgumentException("Unknown expression block: " + block.getClass().getSimpleName());
    }

    public static ValueDto toValueDto(ExpressionBlock block) {
        return toValueDto(block, null);
    }
}
