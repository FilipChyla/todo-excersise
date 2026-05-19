package com.github.filipchyla.todomanager.task.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreationDto {
    @NotBlank(message = "Description should not be blank")
    private String description;
    @NotNull(message = "Due to should not be null")
    private LocalDateTime dueTo;
}
