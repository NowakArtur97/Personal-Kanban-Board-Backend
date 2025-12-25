package com.nowakartur97.personalkanbanboardbackend.common.test;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BaseTaskDeletionMutationControllerTest<E extends BaseTaskEntity> extends TaskSubscriptionIntegrationTest<E> {

    private final int taskIdErrorSourceLocationColumn;

    public BaseTaskDeletionMutationControllerTest(String path, String document, RequestVariable requestVariable, int taskIdErrorSourceLocationColumn,
                                                  String subscriptionDocument, String subscriptionPath, Class<?> subscriptionEntityType) {
        super(path, document, requestVariable, subscriptionDocument, subscriptionPath, subscriptionEntityType);
        this.taskIdErrorSourceLocationColumn = taskIdErrorSourceLocationColumn;
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    public void whenDeleteExistingTask_shouldReturnEmptyResponse(UserRole role) {

        UserEntity userEntity = createUser(role);
        E taskEntity = createTask(userEntity);

        assertTaskDeletionAndSubscription(userEntity, taskEntity.getId());
    }

    @Test
    public void whenDeleteNotExistingTask_shouldReturnEmptyResponse() {

        UserEntity userEntity = createUser();

        assertTaskDeletionAndSubscription(userEntity, UUID.randomUUID());
    }

    @Test
    public void whenDeleteTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, null), new SourceLocation(1, taskIdErrorSourceLocationColumn),
                "Variable '" + requestVariable.getName() + "' has an invalid value: Variable '" + requestVariable.getName() + "' has coerced Null value for NonNull type 'UUID!'");
    }

    protected abstract E createTask(UserEntity userEntity);

    protected abstract void sendDeleteTaskRequest(UserEntity userEntity, UUID taskId);

    protected void assertTaskDeletionAndSubscription(UserEntity userEntity, UUID taskId) {
        Flux<BaseTaskEvent> eventFlux = createWebSocketGraphQlTester(userEntity)
                .document(subscriptionDocument)
                .executeSubscription().toFlux()
                .map(r -> (BaseTaskEvent) r.path(subscriptionPath).entity(subscriptionEntityType).get());

        Mono<UUID> mutationMono = Mono.fromCallable(() -> {
            sendDeleteTaskRequest(userEntity, taskId);
            return taskId;
        });

        Flux<Tuple2<Long, BaseTaskEvent>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        repository.count()
                                .map(count -> Tuples.of(count, tuple.getT1()))
                );
        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isZero();
                    assertThat(tuple.getT2().getTaskEventType()).isEqualTo(TaskEventType.DELETE);
                    assertThat(tuple.getT2().getTaskId()).isEqualTo(taskId);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
