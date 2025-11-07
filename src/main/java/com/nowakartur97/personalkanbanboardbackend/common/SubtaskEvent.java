package com.nowakartur97.personalkanbanboardbackend.common;

import com.nowakartur97.personalkanbanboardbackend.subtask.SubtaskResponse;

public class SubtaskEvent extends BaseTaskEvent<SubtaskResponse> {

    public SubtaskEvent(SubtaskResponse taskResponse, TaskEventType taskEventType) {
        super(taskResponse, taskEventType);
    }
}
