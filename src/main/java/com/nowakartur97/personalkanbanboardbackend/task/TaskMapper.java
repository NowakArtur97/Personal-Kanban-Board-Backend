package com.nowakartur97.personalkanbanboardbackend.task;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class TaskMapper extends BaseTaskMapper<TaskEntity, TaskResponse> {

    @Override
    public TaskEntity mapToEntity(UUID taskId, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        TaskEntity taskEntity = new TaskEntity();
        setEntityFields(taskEntity, taskDTO, createdBy, assignedTo);
        taskEntity.setTaskId(taskId);
        return taskEntity;
    }

    @Override
    public TaskResponse mapToResponse(TaskEntity taskEntity, String createdBy, String updatedBy, String assignedTo) {
        return (TaskResponse) mapToResponse(taskEntity, taskEntity.getTaskId(), null, createdBy, updatedBy, assignedTo);
    }
}
