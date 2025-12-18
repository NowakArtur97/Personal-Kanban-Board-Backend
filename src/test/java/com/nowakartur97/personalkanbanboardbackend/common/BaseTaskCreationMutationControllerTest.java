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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BaseTaskCreationMutationControllerTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskMutationTest<R> {

    @Setter
    private BaseTaskRepository<E> repository;

    protected BaseTaskCreationMutationControllerTest(String path, String document, RequestVariable requestVariable, int validationErrorSourceLocationColumn,
                                                     String subscriptionDocument, String subscriptionPath,
                                                     Class<? extends BaseTaskEvent<? extends BaseTaskResponse>> subscriptionEntityType) {
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

    private void assertTaskMutationAndSubscription(UserEntity userEntity, UserEntity assignedTo, TaskDTO taskDTO) {
        Flux<BaseTaskEvent<R>> eventFlux = createWebSocketGraphQlTester(userEntity)
                .document(subscriptionDocument)
                .executeSubscription().toFlux()
                .map(r -> (BaseTaskEvent<R>) r.path(subscriptionPath).entity(subscriptionEntityType).get());

        Mono<R> mutationMono = Mono.fromCallable(() -> sendCreateTaskRequest(userEntity, taskDTO));

        Flux<Tuple3<BaseTaskEvent<R>, R, E>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        repository.findById(tuple.getT2().getId())
                                .map(entity -> Tuples.of(tuple.getT1(), tuple.getT2(), entity))
                );

        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    BaseTaskEvent<R> taskEvent = tuple.getT1();
                    R taskResponse = tuple.getT2();
                    E taskEntity = tuple.getT3();
                    assertTaskEntity(taskEntity, taskDTO, userEntity.getUserId(), assignedTo.getUserId());
                    assertTaskResponse(taskResponse, taskDTO, userEntity.getUsername(), assignedTo.getUsername());
                    assertThat(taskEvent.getTaskEventType()).isEqualTo(TaskEventType.CREATE);
                    assertTaskEventResponse(taskEvent.getTask(), createExpectedSubscriptionResponse(taskEntity, userEntity.getUsername(), assignedTo.getUsername()));
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    protected abstract R sendCreateTaskRequest(UserEntity userEntity, TaskDTO taskDTO);

    protected abstract void assertTaskEntity(E taskEntity, TaskDTO taskDTO, UUID createdBy, UUID assignedTo);

    protected abstract void assertTaskResponse(R taskResponse, TaskDTO taskDTO, String createdBy, String assignedTo);

    protected abstract void assertTaskEventResponse(R mutationTaskResponse, R subscriptionTaskResponse);

    protected abstract R createExpectedSubscriptionResponse(E taskEntity, String createdBy, String assignedTo);
}
