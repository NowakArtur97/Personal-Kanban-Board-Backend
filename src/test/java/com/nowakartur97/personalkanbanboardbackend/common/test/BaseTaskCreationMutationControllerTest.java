package com.nowakartur97.personalkanbanboardbackend.common.test;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.UUID;

public abstract class BaseTaskCreationMutationControllerTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskMutationTest<E, R> {

    protected BaseTaskCreationMutationControllerTest(String path, String document, RequestVariable requestVariable, int validationErrorSourceLocationColumn,
                                                     String subscriptionDocument, String subscriptionPath,
                                                     Class<?> subscriptionEntityType) {
        super(path, document, requestVariable, validationErrorSourceLocationColumn, subscriptionDocument, subscriptionPath, subscriptionEntityType);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenCreateTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        assertTaskMutationAndSubscription(userEntity, assignedTo, taskDTO);
    }

    @Test
    public void whenCreateTask_shouldCreateTaskWithDefaultValuesAndReturnTaskResponse() {

        UserEntity userEntity = createUser();
        TaskDTO taskDTO = new TaskDTO("title", "description", null, null, null, null);

        assertTaskMutationAndSubscription(userEntity, userEntity, taskDTO);
    }

    protected abstract R sendCreateTaskRequest(UserEntity userEntity, TaskDTO taskDTO);

    private void assertTaskMutationAndSubscription(UserEntity userEntity, UserEntity assignedTo, TaskDTO taskDTO) {
        assertTaskMutationAndSubscription(userEntity, sendCreateTaskRequest(userEntity, taskDTO), TaskEventType.CREATE,
                (taskEntity, taskResponse, taskEvent) -> {
                    assertTaskEntity(taskEntity, taskDTO, userEntity.getUserId(), assignedTo.getUserId());
                    assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername(), assignedTo.getUsername());
                    assertTaskEventResponse(taskEvent.getTask(), createExpectedSubscriptionResponse(taskEntity, userEntity.getUsername(), assignedTo.getUsername()));
                });
    }

    protected abstract void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo);

    protected abstract void assertTaskResponse(R taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo);

    protected abstract R createExpectedSubscriptionResponse(E taskEntity, String createdBy, String assignedTo);
}
