package com.github.filipchyla.todomanager;

import com.github.filipchyla.todomanager.user.User;
import com.github.filipchyla.todomanager.user.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFailWhenEmailIsNotUnique() {
        // Arrange
        User user1 = new User();
        user1.setEmail("alice@example.com");
        user1.setPassword("StrongPass123!");
        entityManager.persist(user1);
        entityManager.flush();

        // Act
        User user2 = new User();
        user2.setEmail("alice@example.com");
        user2.setPassword("differentStrongPass123!");

        // Assert
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    void shouldFailWhenPasswordIsTooWeak() {
        // Arrange
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("password123");

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(user));
    }

    @Test
    void shouldAcceptStrongPassword() {
        // Arrange
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("StrongPass123!");

        // Act & Assert
        assertDoesNotThrow(() -> userRepository.saveAndFlush(user));
    }

    @Test
    void shouldReturnUserByEmail(){
        // Arrange
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("StrongPass123!");
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        // Act
        User foundUser = userRepository.findByEmail("alice@example.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Assert
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertNotNull(foundUser.getUuid());
    }
}
