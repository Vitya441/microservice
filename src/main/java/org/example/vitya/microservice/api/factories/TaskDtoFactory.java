package org.example.vitya.microservice.api.factories;

import org.example.vitya.microservice.api.dto.TaskDto;
import org.example.vitya.microservice.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoFactory {

    public TaskDto makeTaskDto(TaskEntity entity) {

        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .build();

    }
}
