package com.github.filipchyla.todomanager.task.service;

import com.github.filipchyla.todomanager.shared.exception.ResourceNotFoundException;
import com.github.filipchyla.todomanager.task.Task;
import com.github.filipchyla.todomanager.task.TaskRepository;
import com.github.filipchyla.todomanager.task.dto.TaskCreationDto;
import com.github.filipchyla.todomanager.task.dto.TaskInfoDto;
import com.github.filipchyla.todomanager.task.dto.TaskUpdateDto;
import com.github.filipchyla.todomanager.user.User;
import com.github.filipchyla.todomanager.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskInfoDto createTask(TaskCreationDto userTask, User user) {
        Task newTask = new Task();
        newTask.setDescription(userTask.getDescription());
        newTask.setDueTo(userTask.getDueTo());
        newTask.setDone(false);

        User owner = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + user.getEmail()));
        newTask.setOwner(owner);

        return mapToTaskInfoDto(taskRepository.save(newTask));
    }

    @Transactional
    public TaskInfoDto updateTask(UUID taskId,TaskUpdateDto updateInfo, User user) {
        Task task = getTaskIfUserHasAccessElseThrow(taskId, user);

        if (updateInfo.getDescription() != null) task.setDescription(updateInfo.getDescription());
        if (updateInfo.getDueTo() != null)       task.setDueTo(updateInfo.getDueTo());
        if (updateInfo.getIsDone() != null)      task.setDone(updateInfo.getIsDone());

        return mapToTaskInfoDto(task);
    }

    @Transactional
    public void deleteTaskById(UUID id, User user) {
        taskRepository.findById(id).ifPresent(task -> {
            if (!task.getOwner().getEmail().equals(user.getEmail())) {
                throw new AccessDeniedException("You don't have access to this task");
            }
            taskRepository.deleteById(id);
        });
    }

    @Transactional(readOnly = true)
    public TaskInfoDto getTask(UUID id, User user) {
        Task task = getTaskIfUserHasAccessElseThrow(id, user);
        return mapToTaskInfoDto(task);
    }

    @Transactional(readOnly = true)
    public List<TaskInfoDto> getAllTasks(User user) {
        return taskRepository.findByOwnerEmail(user.getEmail()).stream().map(this::mapToTaskInfoDto).toList();
    }

    private Task getTaskIfUserHasAccessElseThrow(UUID id, User user) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Task with ID " + id + " not found"));

        if (!task.getOwner().getEmail().equals(user.getEmail())) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        return task;
    }

    private TaskInfoDto mapToTaskInfoDto(Task task) {
        return TaskInfoDto.builder()
                .id(task.getId())
                .ownerId(task.getOwner().getId())
                .description(task.getDescription())
                .isDone(task.isDone())
                .dueTo(task.getDueTo())
                .timestamp(task.getCreatedAt())
                .build();
    }
}
