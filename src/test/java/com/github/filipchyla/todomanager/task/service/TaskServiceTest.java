package com.github.filipchyla.todomanager.task.service;

import com.github.filipchyla.todomanager.shared.exception.ResourceNotFoundException;
import com.github.filipchyla.todomanager.task.Task;
import com.github.filipchyla.todomanager.task.TaskRepository;
import com.github.filipchyla.todomanager.task.dto.TaskCreationDto;
import com.github.filipchyla.todomanager.task.dto.TaskInfoDto;
import com.github.filipchyla.todomanager.task.dto.TaskUpdateDto;
import com.github.filipchyla.todomanager.user.User;
import com.github.filipchyla.todomanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    private final String TASK_DESCRIPTION = "Test task";
    private final LocalDateTime TASK_DUE_DATE = LocalDateTime.of(2026, 12, 31, 23, 59);
    private final LocalDateTime TASK_CREATION = LocalDateTime.of(2026, 5, 1, 12, 0);
    private final UUID TASK_ID = UUID.randomUUID();

    private final UUID USER_ID = UUID.randomUUID();
    private final String USER_EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);

        task = new Task();
        task.setId(TASK_ID);
        task.setOwner(user);
    }

    @Nested
    class CreateTaskTests {
        private TaskCreationDto creationDto;

        @BeforeEach
        void setUp() {
            creationDto = new TaskCreationDto(TASK_DESCRIPTION, TASK_DUE_DATE);
        }

        @Test
        void shouldCreateTaskAndReturnTaskInfoDto() {
            //Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            //Act
            TaskInfoDto taskInfoDto = taskService.createTask(creationDto, user);

            //Assert
            assertThat(taskInfoDto.getDescription()).isEqualTo(TASK_DESCRIPTION);
            assertThat(taskInfoDto.getDueTo()).isEqualTo(TASK_DUE_DATE);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void shouldThrowExceptionWithNonExistingUser(){
            //Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            //Act & Assert
            assertThatThrownBy(() -> taskService.createTask(creationDto, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with email:");

            verify(taskRepository, times(0)).save(any(Task.class));
        }
    }

    @Nested
    class GetTaskAndAccessControlTests{

        @Test
        void shouldGetTaskAndReturnTaskInfoDto(){
            //Arrange
            task.setDescription(TASK_DESCRIPTION);
            task.setDueTo(TASK_DUE_DATE);
            task.setDone(false);
            task.setCreatedAt(TASK_CREATION);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            //Act
            TaskInfoDto taskInfoDto = taskService.getTask(TASK_ID, user);

            //Assert
            verify(taskRepository, times(1)).findById(TASK_ID);

            assertThat(taskInfoDto.getId()).isEqualTo(TASK_ID);
            assertThat(taskInfoDto.getOwnerId()).isEqualTo(USER_ID);
            assertThat(taskInfoDto.getTimestamp()).isEqualTo(TASK_CREATION);
            assertThat(taskInfoDto.getDescription()).isEqualTo(TASK_DESCRIPTION);
            assertThat(taskInfoDto.getDueTo()).isEqualTo(TASK_DUE_DATE);
            assertThat(taskInfoDto.isDone()).isFalse();
        }

        @Test
        void shouldThrowExceptionWhenUserDoesNotHaveAccessToTask(){
            //Arrange
            User otherUser = new User();
            otherUser.setEmail("another.user@example.com");

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            //Act & Assert
            assertThatThrownBy(() -> taskService.getTask(TASK_ID, otherUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You don't have access to this task");
        }

        @Test
        void shouldThrowExceptionWhenTaskDoesNotExist(){
            //Arrange
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

            //Act & Assert
            assertThatThrownBy(() -> taskService.getTask(TASK_ID, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task with ID");
        }
    }

    @Nested
    class UpdateTaskTests{

        @Test
        void shouldUpdateTaskIfUserHasAccess(){
            //Arrange
            TaskUpdateDto updateDto = new TaskUpdateDto(TASK_DESCRIPTION, TASK_DUE_DATE);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            //Act
            TaskInfoDto taskInfo = taskService.updateTask(TASK_ID, updateDto, user);

            //Assert
            assertThat(taskInfo.getDescription()).isEqualTo(TASK_DESCRIPTION);
            assertThat(taskInfo.getDueTo()).isEqualTo(TASK_DUE_DATE);

            assertThat(task.getDescription()).isEqualTo(taskInfo.getDescription());
            assertThat(task.getDueTo()).isEqualTo(taskInfo.getDueTo());

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void shouldChangeCompletionToFalseIfUserHasAccess(){
            //Arrange
            task.setDone(true);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            //Act
            TaskInfoDto taskInfo = taskService.changeCompletion(TASK_ID, false, user);

            //Assert
            assertThat(taskInfo.isDone()).isFalse();
            assertThat(task.isDone()).isFalse();

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void shouldChangeCompletionToTrueIfUserHasAccess(){
            //Arrange
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            //Act
            TaskInfoDto taskInfo = taskService.changeCompletion(TASK_ID, true, user);

            //Assert
            assertThat(taskInfo.isDone()).isTrue();
            assertThat(task.isDone()).isTrue();

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    class DeleteTaskTests{

        @Test
        void shouldDeleteTaskWhenUserHasAccessAndTaskExists(){
            //Arrange
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            //Act
            taskService.deleteTaskById(TASK_ID, user);

            //Assert
            verify(taskRepository, times(1)).delete(any(Task.class));
            verify(taskRepository, never()).save(any(Task.class));
        }
    }
}
