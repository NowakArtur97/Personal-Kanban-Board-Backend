package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BaseTaskUpdateMutationControllerTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskMutationTest {

    private final String className;
    protected final String idFieldName;
    private final int taskIdErrorSourceLocationColumn;

    @Setter
    private BaseTaskRepository<E> repository;

    protected BaseTaskUpdateMutationControllerTest(String path, String document, RequestVariable requestVariable, int validationErrorSourceLocationColumn,
                                                   String className, String idFieldName, int taskIdErrorSourceLocationColumn) {
        super(path, document, requestVariable, validationErrorSourceLocationColumn);
        this.className = className;
        this.idFieldName = idFieldName;
        this.taskIdErrorSourceLocationColumn = taskIdErrorSourceLocationColumn;
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenUpdateTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity updatedBy = createUser(role);
        UserEntity author = createUser("author", "author@domain.com");
        E taskEntity = createTask(author);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        R taskResponse = sendUpdateTaskRequest(updatedBy, taskEntity, taskDTO);

        E updatedTaskEntity = repository.findAll().blockLast();
        assertTaskEntity(updatedTaskEntity, taskDTO, author, updatedBy, assignedTo);
        assertTaskId(updatedTaskEntity, taskEntity);
        assertTaskResponse(taskResponse, updatedTaskEntity, taskDTO, author.getUsername(), updatedBy.getUsername(), assignedTo.getUsername());
    }

    @Test
    public void whenUpdateAuthorsOwnTask_shouldReturnTaskResponse() {

        UserEntity userEntity = createUser();
        E taskEntity = createTask(userEntity);
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), userEntity.getUserId());

        R taskResponse = sendUpdateTaskRequest(userEntity, taskEntity, taskDTO);

        E updatedTaskEntity = repository.findAll().blockLast();
        assertTaskEntity(updatedTaskEntity, taskDTO, userEntity.getUserId(), userEntity.getUserId(), userEntity.getUserId());
        assertTaskId(updatedTaskEntity, taskEntity);
        assertTaskResponse(taskResponse, updatedTaskEntity, taskDTO, userEntity.getUsername());
    }

    @Test
    public void whenUpdateTask_shouldUpdateTaskWithDefaultValuesAndReturnTaskResponse() {

        UserEntity userEntity = createUser();
        E taskEntity = createTask(userEntity);
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        R taskResponse = sendUpdateTaskRequest(userEntity, taskEntity, taskDTO);

        E updatedTaskEntity = repository.findAll().blockLast();
        assertTaskEntity(updatedTaskEntity, taskDTO, userEntity.getUserId());
        assertTaskId(updatedTaskEntity, taskEntity);
        assertTaskResponse(taskResponse, repository.findAll().blockFirst(), taskDTO, userEntity.getUsername(), userEntity.getUsername());
    }

    @Test
    public void whenUpdateNotExistingTask_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        UUID taskId = UUID.randomUUID();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, UUID.randomUUID());
        DoubleRequestVariable doubleRequestVariable = new DoubleRequestVariable(idFieldName, taskId, requestVariable.getName(), taskDTO);

        assertNotFoundErrorResponse(sendRequestWithErrors(userEntity, document, doubleRequestVariable), path, className + " with " + idFieldName + ": '" + taskId + "' not found.");
    }

    @Test
    public void whenUpdateTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);
        RequestVariable reqVariable = new RequestVariable(requestVariable.getName(), taskDTO);

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, reqVariable), new SourceLocation(1, taskIdErrorSourceLocationColumn),
                "Variable '" + idFieldName + "' has an invalid value: Variable '" + idFieldName + "' has coerced Null value for NonNull type 'UUID!'");
    }

    protected abstract E createTask(UserEntity userEntity);

    protected abstract R sendUpdateTaskRequest(UserEntity userEntity, E taskEntity, TaskDTO taskDTO);

    protected abstract void assertTaskId(E updatedTaskEntity, E taskEntity);

    private void assertTaskEntity(E updatedTaskEntity, TaskDTO taskDTO, UserEntity createdBy, UserEntity updatedBy, UserEntity assignedTo) {
        assertTaskEntity(updatedTaskEntity, taskDTO, createdBy.getUserId(), updatedBy.getUserId(), assignedTo.getUserId());
    }

    private void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID updatedBy, UUID assignedTo) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, updatedBy, assignedTo, taskDTO.getStatus(), taskDTO.getPriority());
    }

    private void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID updatedBy, UUID assignedTo,
                                  TaskStatus taskStatus, TaskPriority taskPriority) {
        assertThat(taskEntity).isNotNull();
        assertThat(taskEntity.getTitle()).isEqualTo(taskDTO.getTitle());
        assertThat(taskEntity.getStatus()).isEqualTo(taskStatus);
        assertThat(taskEntity.getPriority()).isEqualTo(taskPriority);
        assertThat(taskEntity.getTargetEndDate()).isEqualTo(taskDTO.getTargetEndDate());
        assertThat(taskEntity.getAssignedTo()).isEqualTo(assignedTo);
        assertThat(taskEntity.getCreatedOn()).isEqualTo(taskEntity.getCreatedOn());
        assertThat(taskEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(taskEntity.getUpdatedOn()).isNotNull();
        assertThat(taskEntity.getUpdatedBy()).isEqualTo(updatedBy);
    }

    private void assertTaskResponse(R taskResponse, E taskEntity, TaskDTO taskDTO, String createdBy) {
        assertTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, createdBy, createdBy, taskDTO.getStatus(), taskDTO.getPriority());
    }

    private void assertTaskResponse(R taskResponse, E taskEntity, TaskDTO taskDTO, String createdBy, String updatedBy) {
        assertTaskResponse(taskResponse, taskEntity, taskDTO, createdBy, updatedBy, updatedBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    private void assertTaskResponse(R taskResponse, E updatedTaskEntity, TaskDTO taskDTO, String createdBy, String updatedBy, String assignedTo) {
        assertTaskResponse(taskResponse, updatedTaskEntity, taskDTO, createdBy, updatedBy, assignedTo, taskDTO.getStatus(), taskDTO.getPriority());
    }

    protected abstract void assertTaskResponse(R taskResponse, E taskEntity, TaskDTO taskDTO, String createdBy,
                                               String updatedBy, String assignedTo, TaskStatus status, TaskPriority priority);
}
