package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskMapper;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class SubtaskMapper extends BaseTaskMapper<SubtaskEntity, SubtaskResponse> {

    @Override
    public SubtaskEntity mapToEntity(UUID taskId, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        SubtaskEntity subtaskEntity = new SubtaskEntity();
        setEntityFields(subtaskEntity, taskDTO, createdBy, assignedTo);
        subtaskEntity.setTaskId(taskId);
        return subtaskEntity;
    }

    @Override
    public SubtaskResponse mapToResponse(SubtaskEntity subtaskEntity, String createdBy, String updatedBy, String assignedTo) {
        SubtaskResponse subtaskResponse = new SubtaskResponse();
        setResponseFields(subtaskResponse, subtaskEntity, createdBy, updatedBy, assignedTo);
        subtaskResponse.setSubtaskId(subtaskEntity.getSubtaskId());
        subtaskResponse.setTaskId(subtaskEntity.getTaskId());
        return subtaskResponse;
    }
}
