package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Entity
@Table(name = "tasks", schema = "personal_kanban_board")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TaskEntity extends BaseTaskEntity {

    @Id
    @Column(name = "task_id", updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID taskId;

    @Override
    public String toString() {
        return "TaskEntity{" +
                "taskId=" + taskId +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", priority=" + getPriority() +
                ", targetEndDate=" + getTargetEndDate() +
                ", assignedTo=" + getAssignedTo() +
                '}';
    }

    @Override
    public UUID getId() {
        return getTaskId();
    }
}
