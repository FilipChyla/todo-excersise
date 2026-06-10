package com.github.filipchyla.todomanager.task.controller;

import com.github.filipchyla.todomanager.auth.service.JwtService;
import com.github.filipchyla.todomanager.security.config.SecurityConfiguration;
import com.github.filipchyla.todomanager.shared.exception.ResourceNotFoundException;
import com.github.filipchyla.todomanager.task.dto.TaskCreationDto;
import com.github.filipchyla.todomanager.task.dto.TaskInfoDto;
import com.github.filipchyla.todomanager.task.dto.TaskUpdateDto;
import com.github.filipchyla.todomanager.task.service.TaskService;
import com.github.filipchyla.todomanager.user.User;
import com.github.filipchyla.todomanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfiguration.class)
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    private User mockUser;
    private TaskInfoDto taskInfoDto;
    private TaskCreationDto taskCreationDto;

    private final UUID TASK_ID = UUID.randomUUID();
    private final String TASK_DESCRIPTION = "Test Task";
    private final LocalDateTime TASK_DUE_DATE = LocalDateTime.of(2026, 12, 31, 23, 59,0);

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("user@email.com");
        mockUser.setPassword("password");
    }

    @Nested
    class GetTasksTests {

        @BeforeEach
        void setUp() {
            taskInfoDto = TaskInfoDto.builder().id(TASK_ID).description(TASK_DESCRIPTION).build();
        }

        @Test
        void shouldReturnTaskWhenTaskExistsAndUserHasAccess() throws Exception {
            // Arrange
            when(taskService.getTask(eq(TASK_ID), any(User.class))).thenReturn(taskInfoDto);

            // Act & Assert
            mockMvc.perform(get("/api/task/{id}", TASK_ID)
                            .with(user(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
                    .andExpect(jsonPath("$.description").value(TASK_DESCRIPTION));
        }

        @Test
        void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/task/{id}", TASK_ID))
                    .andExpect(status().isUnauthorized());

            verify(taskService, never()).getTask(eq(TASK_ID), any(User.class));
        }

        @Test
        void shouldReturn403WhenTaskDoesNotBelongToUser() throws Exception {
            // Arrange
            when(taskService.getTask(eq(TASK_ID), any(User.class))).thenThrow(AccessDeniedException.class);

            // Act & Assert
            mockMvc.perform(get("/api/task/{id}", TASK_ID)
                            .with(user(mockUser)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn404WhenTaskDoesNotExist() throws Exception {
            // Arrange
            when(taskService.getTask(eq(TASK_ID), any(User.class))).thenThrow(ResourceNotFoundException.class);

            // Act & Assert
            mockMvc.perform(get("/api/task/{id}", TASK_ID)
                            .with(user(mockUser)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class AddTaskTests {
        @BeforeEach
        void setUp() {
            taskCreationDto = new TaskCreationDto(TASK_DESCRIPTION, TASK_DUE_DATE);
            taskInfoDto = TaskInfoDto.builder().id(TASK_ID).description(TASK_DESCRIPTION).dueTo(TASK_DUE_DATE).build();
        }

        @Test
        void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
            //Act & Assert
            mockMvc.perform(post("/api/task")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskCreationDto)))
                    .andExpect(status().isUnauthorized());

            verify(taskService, never()).createTask(any(TaskCreationDto.class), eq(mockUser));
        }

        @Test
        void shouldAddTaskWhenGivenCompulsoryFields() throws Exception {
            //Arrange
            when(taskService.createTask(any(TaskCreationDto.class), any(User.class))).thenReturn(taskInfoDto);

            //Act & Assert
            mockMvc.perform(post("/api/task")
                            .with(user(mockUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskCreationDto)))
                    .andExpect(header().string("Location", endsWith("/api/task/" + TASK_ID)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.description").value(TASK_DESCRIPTION))
                    .andExpect(jsonPath("$.dueTo").value(TASK_DUE_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.done").value(false));

            verify(taskService, times(1)).createTask(any(TaskCreationDto.class), eq(mockUser));
        }
    }

    @Nested
    class DeleteTaskTests {
        @Test
        void shouldReturn204WhenUserHasAccessAndTaskDoesNotExist() throws Exception {
            //Act & Assert
            mockMvc.perform(delete("/api/task/{id}", TASK_ID)
                            .with(user(mockUser)))
                    .andExpect(status().isNoContent());

            verify(taskService, times(1)).deleteTaskById(TASK_ID, mockUser);
        }

        @Test
        void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
            //Act & Assert
            mockMvc.perform(delete("/api/task/{id}", TASK_ID))
                    .andExpect(status().isUnauthorized());

            verify(taskService, never()).deleteTaskById(TASK_ID, mockUser);
        }
    }

    @Nested
    class UpdateTaskTests {

        @Test
        void shouldUpdateWhenUserHasAccessAndTasksExist() throws Exception {
            //Arrange
            taskInfoDto = TaskInfoDto.builder().id(TASK_ID).description(TASK_DESCRIPTION).dueTo(TASK_DUE_DATE).build();
            when(taskService.updateTask(any(UUID.class), any(TaskUpdateDto.class), any(User.class))).thenReturn(taskInfoDto);

            //Act & Assert
            mockMvc.perform(patch("/api/task/{id}", TASK_ID)
                            .with(user(mockUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskUpdateDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value(TASK_DESCRIPTION))
                    .andExpect(jsonPath("$.dueTo").value(TASK_DUE_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.done").value(false));


            verify(taskService, times(1)).updateTask(eq(TASK_ID), any(TaskUpdateDto.class), eq(mockUser));
        }

        @Test
        void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
            //Arrange
            taskInfoDto = TaskInfoDto.builder().id(TASK_ID).description(TASK_DESCRIPTION).dueTo(TASK_DUE_DATE).build();

            //Act & Assert
            mockMvc.perform(patch("/api/task/{id}", TASK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskUpdateDto())))
                    .andExpect(status().isUnauthorized());


            verify(taskService, never()).updateTask(eq(TASK_ID), any(TaskUpdateDto.class), eq(mockUser));
        }
    }
}