package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import graphql.language.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
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

        Flux<UUID> eventFlux = createWebSocketGraphQlTester(userEntity)
                .document(subscriptionDocument)
                .executeSubscription().toFlux()
                .map(r -> (UUID) r.path(subscriptionPath).entity(subscriptionEntityType).get());

        Mono<UUID> mutationMono = Mono.fromCallable(() -> sendDeleteTaskRequest(userEntity));

        Flux<Tuple3<UUID, UUID, Long>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        repository.count()
                                .map(entity -> Tuples.of(tuple.getT1(), tuple.getT2(), entity))
                );
        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    assertThat(tuple.getT3()).isZero();
                    UUID taskEvent = tuple.getT1();
                    assertThat(taskEvent).isEqualTo(tuple.getT2());
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    public void whenDeleteNotExistingTask_shouldReturnEmptyResponse() {

        UserEntity userEntity = createUser();

        sendDeleteTaskRequest(userEntity, UUID.randomUUID());

        assertThat(taskRepository.count().block()).isZero();
    }

    @Test
    public void whenDeleteTaskWithoutTaskId_shouldReturnGraphQLErrorResponse() {

        UserEntity userEntity = createUser();

        assertValidationErrorResponse(sendRequestWithErrors(userEntity, document, null), new SourceLocation(1, taskIdErrorSourceLocationColumn),
                "Variable '" + requestVariable.getName() + "' has an invalid value: Variable '" + requestVariable.getName() + "' has coerced Null value for NonNull type 'UUID!'");
    }

    private UUID sendDeleteTaskRequest(UserEntity userEntity) {
        UUID taskId = UUID.randomUUID();
        sendDeleteTaskRequest(userEntity, taskId);
        return taskId;
    }

    protected abstract void sendDeleteTaskRequest(UserEntity userEntity, UUID taskId);
}
