package com.nowakartur97.personalkanbanboardbackend.common;

public class TaskSubscriptionIntegrationTest<E extends BaseTaskEntity> extends TaskIntegrationTest<E> {

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
}
