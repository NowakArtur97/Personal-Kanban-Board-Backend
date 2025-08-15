package com.nowakartur97.personalkanbanboardbackend.subtask;

import com.nowakartur97.personalkanbanboardbackend.common.BaseTaskMapper;
import com.nowakartur97.personalkanbanboardbackend.task.TaskDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class SubtaskMapper extends BaseTaskMapper<SubtaskEntity, SubtaskResponse> {

    @Override
    public SubtaskEntity mapToEntity(UUID taskId, TaskDTO taskDTO, UUID createdBy, UUID assignedTo) {
        return (SubtaskEntity) mapToEntity(false, taskId, taskDTO, createdBy, assignedTo);
    }

    @Override
    public SubtaskResponse mapToResponse(SubtaskEntity subtaskEntity, String createdBy, String updatedBy, String assignedTo) {
        return (SubtaskResponse) mapToResponse(subtaskEntity, subtaskEntity.getTaskId(), subtaskEntity.getSubtaskId(), createdBy, updatedBy, assignedTo);
    }
}
