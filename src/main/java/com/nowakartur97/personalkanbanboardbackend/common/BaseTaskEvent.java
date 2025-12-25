package com.nowakartur97.personalkanbanboardbackend.common;

import java.util.UUID;

public interface BaseTaskEvent {

    TaskEventType getTaskEventType();

    BaseTaskResponse getTask();

    UUID getTaskId();
}
