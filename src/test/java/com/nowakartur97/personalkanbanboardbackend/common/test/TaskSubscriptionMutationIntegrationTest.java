package com.nowakartur97.personalkanbanboardbackend.common.test;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEntity;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskEvent;
import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskResponse;
import com.nowakartur97.personalkanbanboardbackend.common.TaskEventType;
import com.nowakartur97.personalkanbanboardbackend.common.request.RequestVariable;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import org.apache.logging.log4j.util.TriConsumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class TaskSubscriptionMutationIntegrationTest<E extends BaseTaskEntity, R extends BaseTaskResponse> extends TaskSubscriptionIntegrationTest<E> {

    public TaskSubscriptionMutationIntegrationTest(String path, String document, RequestVariable requestVariable,
                                                   String subscriptionDocument, String subscriptionPath, Class<?> subscriptionEntityType) {
        super(path, document, requestVariable, subscriptionDocument, subscriptionPath, subscriptionEntityType);
    }

    protected void assertTaskMutationAndSubscription(UserEntity userEntity, R request, TaskEventType taskEventType, TriConsumer<E, R, BaseTaskEvent<R>> assertions) {
        Flux<BaseTaskEvent<R>> eventFlux = createWebSocketGraphQlTester(userEntity)
                .document(subscriptionDocument)
                .executeSubscription().toFlux()
                .map(r -> (BaseTaskEvent<R>) r.path(subscriptionPath).entity(subscriptionEntityType).get());

        Mono<R> mutationMono = Mono.fromCallable(() -> request);

        Flux<Tuple3<E, R, BaseTaskEvent<R>>> combined = eventFlux
                .take(1)
                .zipWith(mutationMono)
                .flatMap(tuple ->
                        repository.findById(tuple.getT2().getId())
                                .map(entity -> Tuples.of(entity, tuple.getT2(), tuple.getT1()))
                );
        StepVerifier.create(combined)
                .assertNext(tuple -> {
                    E taskEntity = tuple.getT1();
                    R taskResponse = tuple.getT2();
                    BaseTaskEvent<R> taskEvent = tuple.getT3();
                    assertThat(taskEvent.getTaskEventType()).isEqualTo(taskEventType);
                    assertions.accept(taskEntity, taskResponse, taskEvent);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
