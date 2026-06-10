package com.github.filipchyla.todomanager.task.controller;

import com.github.filipchyla.todomanager.task.dto.TaskCreationDto;
import com.github.filipchyla.todomanager.task.dto.TaskInfoDto;
import com.github.filipchyla.todomanager.task.dto.TaskUpdateDto;
import com.github.filipchyla.todomanager.task.service.TaskService;
import com.github.filipchyla.todomanager.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskInfoDto>> getAllTasks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getAllTasks(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskInfoDto> getTasksById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getTask(id, user));
    }

    @PostMapping
    public ResponseEntity<TaskInfoDto> addNewTask(@Valid @RequestBody TaskCreationDto newTask, @AuthenticationPrincipal User user) {
        TaskInfoDto createdTask = taskService.createTask(newTask, user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTask);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskInfoDto> updateTask(@PathVariable UUID id, @RequestBody TaskUpdateDto updateInfo, @AuthenticationPrincipal User user) {
        TaskInfoDto updatedTask = taskService.updateTask(id, updateInfo, user);

        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        taskService.deleteTaskById(id, user);
        return ResponseEntity.noContent().build();
    }

}