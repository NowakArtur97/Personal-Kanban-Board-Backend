package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BaseTaskEvent<R extends BaseTaskResponse> {

    private final R task;
    private final TaskEventType taskEventType;
}
