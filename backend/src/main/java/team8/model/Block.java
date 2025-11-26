package team8.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import team8.execution.ExecutionContext;
import team8.execution.ExecutionResult;

@Entity
@Table(name = "blocks")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "block_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;

    @Column(name = "block_order")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    @Column(name = "next_block_id")
    private Long nextBlockId;

    public abstract String getBlockType();

    public abstract ExecutionResult execute(ExecutionContext context);
}
