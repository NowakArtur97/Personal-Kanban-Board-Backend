package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tasks", schema = "personal_kanban_board")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TaskEntity extends Auditable<UUID> {

    @Id
    @Column(name = "task_id", updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID taskId;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(length = 100)
    private String description;
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;
    @Column(name = "target_end_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate targetEndDate;
    @Column(name = "assigned_to", nullable = false)
    private UUID assignedTo;

    @Override
    public String toString() {
        return "TaskEntity{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", targetEndDate=" + targetEndDate +
                ", assignedTo=" + assignedTo +
                '}';
    }
}
