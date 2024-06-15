package com.nowakartur97.personalkanbanboardbackend.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class TaskDTO {

    private String title;
    private String description;
    private TaskPriority priority;
    private LocalDate targetEndDate;
    @Setter
    private UUID assignedTo;
}
