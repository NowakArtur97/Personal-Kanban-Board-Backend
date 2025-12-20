package com.nowakartur97.personalkanbanboardbackend.common;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class TaskSubscriptionIntegrationTest<E extends BaseTaskEntity> extends TaskIntegrationTest<E> {

    protected final String subscriptionDocument;
    protected final String subscriptionPath;
    protected final Class<?> subscriptionEntityType;

    public TaskSubscriptionIntegrationTest(String path, String document, RequestVariable requestVariable,
                                           String subscriptionDocument, String subscriptionPath, Class<?> subscriptionEntityType) {
        super(path, document, requestVariable);
        this.subscriptionDocument = subscriptionDocument;
        this.subscriptionPath = subscriptionPath;
        this.subscriptionEntityType = subscriptionEntityType;
    }

    protected void assertBaseTaskResponse(BaseTaskResponse mutationTaskResponse, BaseTaskResponse subscriptionTaskResponse) {
        assertThat(subscriptionTaskResponse).isNotNull();
        assertThat(subscriptionTaskResponse.getTaskId()).isEqualTo(mutationTaskResponse.getTaskId());
        assertThat(subscriptionTaskResponse.getTitle()).isEqualTo(mutationTaskResponse.getTitle());
        assertThat(subscriptionTaskResponse.getStatus()).isEqualTo(mutationTaskResponse.getStatus());
        assertThat(subscriptionTaskResponse.getPriority()).isEqualTo(mutationTaskResponse.getPriority());
        assertThat(subscriptionTaskResponse.getTargetEndDate()).isEqualTo(mutationTaskResponse.getTargetEndDate());
        assertThat(subscriptionTaskResponse.getAssignedTo()).isEqualTo(mutationTaskResponse.getAssignedTo());
        assertThat(Instant.parse(subscriptionTaskResponse.getCreatedOn()).toEpochMilli()).isEqualTo(Instant.parse(mutationTaskResponse.getCreatedOn()).toEpochMilli());
        assertThat(subscriptionTaskResponse.getCreatedBy()).isEqualTo(mutationTaskResponse.getCreatedBy());
        if (subscriptionTaskResponse.getUpdatedBy() != null) {
            assertThat(Instant.parse(subscriptionTaskResponse.getUpdatedOn()).toEpochMilli()).isEqualTo(Instant.parse(mutationTaskResponse.getUpdatedOn()).toEpochMilli());
        } else {
            assertThat(subscriptionTaskResponse.getUpdatedOn()).isNull();
        }
        assertThat(subscriptionTaskResponse.getUpdatedBy()).isEqualTo(subscriptionTaskResponse.getUpdatedBy());
    }
}
