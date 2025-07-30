package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseTaskEntity extends Auditable<UUID> {

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
}
