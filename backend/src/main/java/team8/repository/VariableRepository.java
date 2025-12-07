package team8.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team8.model.variable.Variable;

import java.util.Optional;

public interface VariableRepository extends JpaRepository<Variable, Long> {
    Optional<Variable> findByProjectIdAndName(Long projectId, String name);
}
