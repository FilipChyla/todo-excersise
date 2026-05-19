package com.github.filipchyla.todomanager.task;

import com.github.filipchyla.todomanager.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TaskRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User user;
    private Task task;
    private final String USER_EMAIL = "alice@example.com";

    @BeforeEach
    void setUp() {
        entityManager.clear();
        user = new User();
        user.setEmail(USER_EMAIL);
        user.setPassword("StrongPass123!");
        entityManager.persist(user);
        entityManager.flush();

        task = new Task();
    }

    @Test
    void shouldNotPersistIfDescriptionIsEmpty(){
        //Arrange
        task.setOwner(user);
        task.setDueTo(null);
        task.setDone(false);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    @Test
    void shouldNotPersistIfOwnerIsNull(){
        //Arrange
        task.setDescription("Test task");
        task.setDueTo(null);
        task.setDone(false);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    @Test
    void shouldCreateTimestampWhenPersistingTask(){
        //Arrange
        task.setOwner(user);
        task.setDueTo(null);
        task.setDone(false);
        task.setDescription("Test task");

        entityManager.persist(task);
        entityManager.flush();
        entityManager.clear();

        //Act
        Task foundTask = taskRepository.findById(task.getId()).orElseThrow();

        // Assert
        assertNotNull(foundTask.getCreatedAt());
    }

    @Test
    void shouldFindAllTasksByOwner(){
        //Arrange
        task.setOwner(user);
        task.setDescription("Test task");
        entityManager.persist(task);

        Task task2 = new Task();
        task2.setOwner(user);
        task2.setDescription("Test task");
        entityManager.persist(task2);

        User user2 = new User();
        user2.setEmail("bob@example.com");
        user2.setPassword("StrongPass123!");
        entityManager.persist(user2);

        Task task3 = new Task();
        task3.setOwner(user2);
        task3.setDescription("Test task");
        entityManager.persist(task3);

        entityManager.flush();
        entityManager.clear();

        //Act
        List<Task> foundTask = taskRepository.findByOwnerEmail(USER_EMAIL);

        // Assert
        assertEquals(2, foundTask.size());
    }
}
