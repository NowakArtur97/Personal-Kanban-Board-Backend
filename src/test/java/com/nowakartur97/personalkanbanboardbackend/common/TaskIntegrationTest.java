package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskEntity;
import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskResponse;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskRepository;
import com.nowakartur97.personalkanbanboardbackend.task.TaskResponse;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class TaskIntegrationTest extends BasicIntegrationTest {

    @Autowired
    protected TaskRepository taskRepository;

    public TaskIntegrationTest(String path, String document, RequestVariable requestVariable) {
        super(path, document, requestVariable);
    }

    @AfterEach
    public void cleanUpTables() {
        taskRepository.deleteAll().block();
    }

    protected TaskEntity createTask(UUID authorId) {
        return createTask(authorId, authorId, null);
    }

    protected TaskEntity createTask(UUID authorId, UUID assignedToId, UUID updatedById) {
        return taskRepository.save(TaskEntity.builder()
                        .title("testTask")
                        .description("test task")
                        .assignedTo(authorId)
                        .status(TaskStatus.IN_PROGRESS)
                        .priority(TaskPriority.MEDIUM)
                        .targetEndDate(LocalDate.now().plusDays(14))
                        .createdOn(Instant.now())
                        .createdBy(assignedToId)
                        .updatedOn(updatedById != null ? Instant.now() : null)
                        .updatedBy(updatedById)
                        .build())
                .block();
    }

    protected SubtaskEntity createSubtask(UUID taskId, UUID authorId) {
        return createSubtask(taskId, authorId, authorId, null);
    }

    protected SubtaskEntity createSubtask(UUID taskId, UUID authorId, UUID assignedToId, UUID updatedById) {
        return subtaskRepository.save(SubtaskEntity.builder()
                        .taskId(taskId)
                        .title("testSubtask")
                        .description("test subtask")
                        .assignedTo(authorId)
                        .status(TaskStatus.READY_TO_START)
                        .priority(TaskPriority.LOW)
                        .targetEndDate(LocalDate.now().plusDays(7))
                        .createdOn(Instant.now())
                        .createdBy(assignedToId)
                        .updatedOn(updatedById != null ? Instant.now() : null)
                        .updatedBy(updatedById)
                        .build())
                .block();
    }

    protected void assertTaskResponse(TaskResponse taskResponse, TaskEntity taskEntity,
                                      String createdBy, String assignedTo, String updatedBy) {
        assertBaseTaskResponse(taskResponse, taskEntity, createdBy, assignedTo, updatedBy);
        assertThat(taskResponse.getTaskId()).isEqualTo(taskEntity.getTaskId());
    }

    protected void assertSubtaskResponse(SubtaskResponse subtaskResponse, SubtaskEntity subtaskEntity,
                                         String createdBy, String assignedTo, String updatedBy) {
        assertBaseTaskResponse(subtaskResponse, subtaskEntity, createdBy, assignedTo, updatedBy);
        assertThat(subtaskResponse.getSubtaskId()).isEqualTo(subtaskEntity.getSubtaskId());
        assertThat(subtaskResponse.getTaskId()).isEqualTo(subtaskEntity.getTaskId());
    }

    private void assertBaseTaskResponse(BaseTaskResponse taskResponse, BaseTaskEntity taskEntity,
                                        String assignedTo, String createdBy, String updatedBy) {
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getTaskId()).isNotNull();
        assertThat(taskResponse.getTitle()).isEqualTo(taskEntity.getTitle());
        assertThat(taskResponse.getStatus()).isEqualTo(taskEntity.getStatus());
        assertThat(taskResponse.getPriority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskResponse.getTargetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskResponse.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(Instant.parse(taskResponse.getCreatedOn()).toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskResponse.getCreatedBy()).isEqualTo(createdBy);
        if (updatedBy != null) {
            assertThat(Instant.parse(taskResponse.getUpdatedOn()).toEpochMilli()).isEqualTo(taskEntity.getUpdatedOn().toEpochMilli());
        } else {
            assertThat(taskResponse.getUpdatedOn()).isNull();
        }
        assertThat(taskResponse.getUpdatedBy()).isEqualTo(updatedBy);
    }

    protected void assertBaseTaskEntity(BaseTaskEntity taskEntity, BaseTaskEntity taskEntityAfterUpdate, UUID assignedTo) {
        assertThat(taskEntityAfterUpdate).isNotNull();
        assertThat(taskEntityAfterUpdate.getTitle()).isEqualTo(taskEntity.getTitle());
        assertThat(taskEntityAfterUpdate.getStatus()).isEqualTo(taskEntity.getStatus());
        assertThat(taskEntityAfterUpdate.getPriority()).isEqualTo(taskEntity.getPriority());
        assertThat(taskEntityAfterUpdate.getTargetEndDate()).isEqualTo(taskEntity.getTargetEndDate());
        assertThat(taskEntityAfterUpdate.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntityAfterUpdate.getCreatedOn().toEpochMilli()).isEqualTo(taskEntity.getCreatedOn().toEpochMilli());
        assertThat(taskEntityAfterUpdate.getCreatedBy()).isEqualTo(taskEntity.getCreatedBy());
        assertThat(taskEntityAfterUpdate.getUpdatedOn()).isAfter(taskEntity.getUpdatedOn());
        assertThat(taskEntityAfterUpdate.getUpdatedBy()).isEqualTo(taskEntity.getUpdatedBy());
    }
}
