package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.UUID;

public abstract class BaseTaskCreationMutationControllerTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskMutationTest {

    @Setter
    private BaseTaskRepository<E> repository;

    protected BaseTaskCreationMutationControllerTest(String path, String document, RequestVariable requestVariable, int sourceLocationColumn) {
        super(path, document, requestVariable, sourceLocationColumn);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenCreateTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        R taskResponse = sendCreateTaskRequest(userEntity, taskDTO);

        assertTaskEntity(repository.findAll().blockLast(), taskDTO, userEntity.getUserId(), assignedTo.getUserId());
        assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername(), assignedTo.getUsername(),
                taskDTO.getStatus(), taskDTO.getPriority());
    }

    @Test
    public void whenCreateTask_shouldCreateTaskWithDefaultValuesAndReturnTaskResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        R taskResponse = sendCreateTaskRequest(userEntity, taskDTO);

        assertTaskEntity(repository.findAll().blockLast(), taskDTO, userEntity.getUserId());
        assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername());
    }

    protected abstract R sendCreateTaskRequest(UserEntity userEntity, TaskDTO taskDTO);

    protected void assertTaskResponse(R taskResponse, TaskDTO taskDTO, String createdBy) {
        assertTaskResponse(taskResponse, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    protected abstract void assertTaskResponse(R taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo,
                                               TaskStatus status, TaskPriority priority);

    protected void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    protected void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, assignedTo, taskDTO.getStatus(), taskDTO.getPriority());
    }

    protected abstract void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo,
                                             TaskStatus taskStatus, TaskPriority taskPriority);
}
