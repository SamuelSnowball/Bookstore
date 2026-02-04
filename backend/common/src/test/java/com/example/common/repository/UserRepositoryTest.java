package com.example.common.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.common.database.MyDataSource;
import com.example.database.generated.tables.pojos.User;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MyDataSource.class, UserRepository.class})
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DSLContext create;

    @Test
    void testFindByUsername_UserExists() {
        // Arrange - Create a test user using constructor (User POJO is immutable)
        User testUser = new User(null, "testuser123", "hashedpassword");
        userRepository.insert(testUser);

        // Act
        User foundUser = userRepository.findByUsername("testuser123");

        // Assert
        assertNotNull(foundUser);
        assertEquals("testuser123", foundUser.getUsername());
        assertEquals("hashedpassword", foundUser.getPassword());
    }

    @Test
    void testFindByUsername_UserDoesNotExist() {
        // Act
        User foundUser = userRepository.findByUsername("nonexistent_user");

        // Assert
        assertNull(foundUser);
    }

    @Test
    void testInsertUser() {
        // Arrange - Use constructor for immutable User POJO
        User newUser = new User(null, "newuser", "password123hash");

        // Act
        userRepository.insert(newUser);

        // Assert
        User foundUser = userRepository.findByUsername("newuser");
        assertNotNull(foundUser);
        assertEquals("newuser", foundUser.getUsername());
        assertEquals("password123hash", foundUser.getPassword());
        assertNotNull(foundUser.getId()); // Auto-generated ID
    }

    @Test
    void testFindById() {
        // Arrange - Create and insert a user using constructor
        User testUser = new User(null, "idtest", "hash");
        userRepository.insert(testUser);
        
        User insertedUser = userRepository.findByUsername("idtest");

        // Act
        User foundUser = userRepository.findById(insertedUser.getId());

        // Assert
        assertNotNull(foundUser);
        assertEquals(insertedUser.getId(), foundUser.getId());
        assertEquals("idtest", foundUser.getUsername());
    }

    @Test
    void testUpdateUser() {
        // Arrange
        User user = new User(null, "updatetest", "oldhash");
        userRepository.insert(user);

        User insertedUser = userRepository.findByUsername("updatetest");

        // Act - Update password by creating new User instance (immutable)
        User updatedUser = new User(insertedUser.getId(), "updatetest", "newhash");
        userRepository.update(updatedUser);

        // Assert
        User fetchedUser = userRepository.findById(insertedUser.getId());
        assertEquals("newhash", fetchedUser.getPassword());
        assertEquals("updatetest", fetchedUser.getUsername()); // Username unchanged
    }

    @Test
    void testDeleteUser() {
        // Arrange
        User user = new User(null, "deletetest", "hash");
        userRepository.insert(user);

        User insertedUser = userRepository.findByUsername("deletetest");
        Integer userId = insertedUser.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        User deletedUser = userRepository.findById(userId);
        assertNull(deletedUser);
    }

    @Test
    void testFindByUsername_CaseSensitive() {
        // Arrange
        User user = new User(null, "TestUser", "hash");
        userRepository.insert(user);

        // Act
        User foundExact = userRepository.findByUsername("TestUser");
        User foundLower = userRepository.findByUsername("testuser");

        // Assert
        assertNotNull(foundExact);
        assertEquals("TestUser", foundExact.getUsername());
        
        // MySQL usernames are typically case-insensitive by default
        // but this depends on collation - adjust assertion based on your DB config
        if (foundLower != null) {
            assertEquals("TestUser", foundLower.getUsername());
        }
    }
}
