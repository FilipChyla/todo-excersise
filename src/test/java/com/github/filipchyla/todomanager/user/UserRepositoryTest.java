package com.github.filipchyla.todomanager.user;

import org.junit.jupiter.api.BeforeEach;
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

    private User user;
    private final String USER_EMAIL = "alice@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(USER_EMAIL);
        user.setPassword("StrongPass123!");
        entityManager.clear();
    }

    @Test
    void shouldFailWhenEmailIsNotUnique() {
        //Arrange
        entityManager.persist(user);
        entityManager.flush();

        User newUser = new User();
        newUser.setEmail(USER_EMAIL);
        newUser.setPassword("differentStrongPass123!");

        //Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(newUser));
    }

    @Test
    void shouldPersistAndFindUserByEmail(){
        // Arrange
        entityManager.persist(user);
        entityManager.flush();

        // Act & Assert
        assertTrue(userRepository.existsByEmail(USER_EMAIL));
    }

    @Test
    void shouldPersistAndReturnUserByEmail(){
        // Arrange
        entityManager.persist(user);
        entityManager.flush();
        // Act
        User foundUser = userRepository.findByEmail(USER_EMAIL)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Assert
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertNotNull(foundUser.getUuid());
    }
}
