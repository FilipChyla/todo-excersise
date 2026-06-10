package com.github.filipchyla.todomanager.task.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TaskUpdateDto {
    private String description;
    private LocalDateTime dueTo;
    private Boolean isDone;
}
