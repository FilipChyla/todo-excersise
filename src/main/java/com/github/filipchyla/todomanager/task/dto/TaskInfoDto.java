package com.github.filipchyla.todomanager.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskInfoDto {
    private UUID id;
    private UUID ownerId;
    private String description;
    private LocalDateTime dueTo;
    private boolean isDone;
    private LocalDateTime timestamp;
}
