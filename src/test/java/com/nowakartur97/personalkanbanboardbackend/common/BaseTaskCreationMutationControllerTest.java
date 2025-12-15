package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BaseTaskCreationMutationControllerTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskMutationTest {

    @Setter
    private BaseTaskRepository<E> repository;

    protected BaseTaskCreationMutationControllerTest(String path, String document, RequestVariable requestVariable, int validationErrorSourceLocationColumn,
                                                     String subscriptionDocument, String subscriptionPath,
                                                     Class<? extends BaseTaskEvent<? extends BaseTaskResponse>> subscriptionEntityType) {
        super(path, document, requestVariable, validationErrorSourceLocationColumn, subscriptionDocument, subscriptionPath, subscriptionEntityType);
    }

    @Test
    @Timeout(10)
    public void whenCreateTaskByUser_shouldReturnTaskResponse() {
        whenCreateTask_shouldReturnTaskResponse(UserRole.USER);
    }

    @Test
    @Timeout(10)
    public void whenCreateTaskByAdmin_shouldReturnTaskResponse() {
        whenCreateTask_shouldReturnTaskResponse(UserRole.ADMIN);
    }

    private void whenCreateTask_shouldReturnTaskResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        UserEntity assignedTo = createUser("developer", "developer@domain.com");
        TaskDTO taskDTO = new TaskDTO("title", "description", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now(), assignedTo.getUserId());

        Flux<BaseTaskEvent> event = createWebSocketGraphQlTester(userEntity)
                .document(subscriptionDocument)
                .executeSubscription().toFlux()
                .map(r -> r.path(subscriptionPath).entity(subscriptionEntityType).get());

        Flux<Map<String, Object>> combined = event
                .take(1)
                .zipWith(Mono.defer(() -> Mono.just(sendCreateTaskRequest(userEntity, taskDTO))))
                .flatMap(tuple -> repository.findById(tuple.getT2().getId())
                        .map(entity -> Map.of("event", tuple.getT1(), "response", tuple.getT2(), "entity", entity)));

        StepVerifier.create(combined)
                .assertNext(map -> {
                    BaseTaskEvent<R> taskEvent = (BaseTaskEvent<R>) map.get("event");
                    R taskResponse = (R) map.get("response");
                    E taskEntity = (E) map.get("entity");
                    assertTaskEntity(taskEntity, taskDTO, userEntity.getUserId(), assignedTo.getUserId());
                    assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername(), assignedTo.getUsername(),
                            taskDTO.getStatus(), taskDTO.getPriority());
                    assertThat(taskEvent.getTaskEventType()).isEqualTo(TaskEventType.CREATE);
                    assertTaskEventResponse(taskEvent.getTask(), createExpectedSubscriptionResponse(taskEntity, userEntity.getUsername(), assignedTo.getUsername()));
                })
                .thenCancel()
                .verify(Duration.ofSeconds(15));
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

    protected abstract R createExpectedSubscriptionResponse(E taskEntity, String createdBy, String assignedTo);

    protected void assertTaskResponse(R taskResponse, TaskDTO taskDTO, String createdBy) {
        assertTaskResponse(taskResponse, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    protected abstract void assertTaskResponse(R taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo,
                                               TaskStatus status, TaskPriority priority);

    protected abstract void assertTaskEventResponse(R mutationTaskResponse, R subscriptionTaskResponse);

    protected void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, createdBy, TaskStatus.READY_TO_START, TaskPriority.LOW);
    }

    protected void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        assertTaskEntity(taskEntity, taskDTO, createdBy, assignedTo, taskDTO.getStatus(), taskDTO.getPriority());
    }

    protected abstract void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo,
                                             TaskStatus taskStatus, TaskPriority taskPriority);
}
