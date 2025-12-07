package team8.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team8.model.expression.ExpressionBlock;

public interface ExpressionRepository extends JpaRepository<ExpressionBlock, Long> {
}
