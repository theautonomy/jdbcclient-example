package com.example.jdbcclient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.dto.UserFilter;
import com.example.jdbcclient.dto.UserSummary;
import com.example.jdbcclient.model.User;
import com.example.jdbcclient.model.UserStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserRepository.class)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    // ==================================================
    // SLIDE: Basic Query Operations
    // ==================================================

    @Test
    void findActiveUsers_shouldReturnOnlyActiveUsers() {
        List<User> activeUsers = userRepository.findActiveUsers();

        assertThat(activeUsers).isNotEmpty();
        assertThat(activeUsers).allMatch(User::isActive);
        assertThat(activeUsers.size())
                .isGreaterThanOrEqualTo(4); // We have 4 active users in test data
    }

    // ==================================================
    // SLIDE: Why JdbcClient? - Modern JdbcClient example
    // ==================================================

    @Test
    void findByIdAndActive_shouldReturnUser_whenUserExistsAndActive() {
        Optional<User> user = userRepository.findByIdAndActive(1L);

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("john@example.com");
        assertThat(user.get().isActive()).isTrue();
    }

    @Test
    void findByIdAndActive_shouldReturnEmpty_whenUserNotActive() {
        Optional<User> user = userRepository.findByIdAndActive(3L); // Bob is inactive

        assertThat(user).isEmpty();
    }

    // ==================================================
    // SLIDE: Parameter Binding
    // ==================================================

    @Test
    void findByEmailAndStatus_shouldReturnUser_whenMatches() {
        Optional<User> user =
                userRepository.findByEmailAndStatus("john@example.com", UserStatus.ACTIVE);

        assertThat(user).isPresent();
        assertThat(user.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByFilter_shouldReturnMatchingUsers() {
        UserFilter filter = new UserFilter("john@example.com", UserStatus.ACTIVE, 18);

        List<User> users = userRepository.findByFilter(filter);

        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getEmail()).isEqualTo("john@example.com");
        assertThat(users.get(0).isActive()).isTrue();
    }

    // ==================================================
    // SLIDE: Result Mapping
    // ==================================================

    @Test
    void findById_shouldReturnUser_whenExists() {
        Optional<User> user = userRepository.findById(1L);

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<User> user = userRepository.findById(999L);

        assertThat(user).isEmpty();
    }

    @Test
    void getUserSummaries_shouldReturnCustomMappedResults() {
        List<UserSummary> summaries = userRepository.getUserSummaries();

        assertThat(summaries).isNotEmpty();
        assertThat(summaries)
                .allMatch(s -> s.id() != null && s.email() != null && s.fullName() != null);

        UserSummary johnSummary =
                summaries.stream()
                        .filter(s -> s.email().equals("john@example.com"))
                        .findFirst()
                        .orElseThrow();
        assertThat(johnSummary.fullName()).isEqualTo("John Doe");
    }

    // ==================================================
    // SLIDE: Insert Operations
    // ==================================================

    @Test
    void insertSimple_shouldInsertNewUser() {
        User newUser = new User("test@example.com", "Test", "User", true);

        int rowsAffected = userRepository.insertSimple(newUser);

        assertThat(rowsAffected).isEqualTo(1);

        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    void insertWithGeneratedKeys_shouldReturnGeneratedId() {
        User newUser = new User("keygen@example.com", "KeyGen", "User", true);

        Long generatedId = userRepository.insertWithGeneratedKeys(newUser);

        assertThat(generatedId).isNotNull().isPositive();

        Optional<User> savedUser = userRepository.findById(generatedId);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("keygen@example.com");
    }

    // ==================================================
    // SLIDE: Update and Delete
    // ==================================================

    @Test
    void updateEmail_shouldModifyUserEmail() {
        Long userId = 1L;
        String newEmail = "newemail@example.com";

        int updatedRows = userRepository.updateEmail(userId, newEmail);

        assertThat(updatedRows).isEqualTo(1);

        Optional<User> updatedUser = userRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo(newEmail);
    }

    @Test
    void deleteInactiveOldUsers_shouldDeleteMatchingUsers() {
        // First, create an old inactive user
        User oldInactiveUser = new User("old@example.com", "Old", "User", false);
        userRepository.insertSimple(oldInactiveUser);

        LocalDateTime cutoffDate = LocalDateTime.now().plusYears(1); // Future date for testing

        int deletedRows = userRepository.deleteInactiveOldUsers(cutoffDate);

        assertThat(deletedRows).isGreaterThanOrEqualTo(1); // Should delete Bob and our test user
    }

    // ==================================================
    // SLIDE: Advanced Features
    // ==================================================

    @Test
    void getUsersByDepartment_shouldGroupUsersByDepartment() {
        Map<String, List<User>> usersByDept = userRepository.getUsersByDepartment();

        assertThat(usersByDept).isNotEmpty();
        assertThat(usersByDept).containsKey("Engineering");
        assertThat(usersByDept.get("Engineering")).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void countActiveUsersViaStream_shouldCountCorrectly() {
        long activeCount = userRepository.countActiveUsersViaStream();

        assertThat(activeCount).isGreaterThanOrEqualTo(4); // We have 4 active users
    }

    // ==================================================
    // SLIDE: Migration from JdbcTemplate
    // ==================================================

    @Test
    void findUsersByAgeRange_shouldReturnUsersInRange() {
        List<User> users = userRepository.findUsersByAgeRange(25, 35);

        assertThat(users).isNotEmpty();
        assertThat(users).allMatch(u -> u.getAge() >= 25 && u.getAge() <= 35);
    }

    // ==================================================
    // SLIDE: Best Practices
    // ==================================================

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        Optional<User> user = userRepository.findByEmail("john@example.com");

        assertThat(user).isPresent();
        assertThat(user.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenNotExists() {
        Optional<User> user = userRepository.findByEmail("nonexistent@example.com");

        assertThat(user).isEmpty();
    }

    // ==================================================
    // SLIDE: Complete Example: User Management
    // ==================================================

    @Test
    void findAll_shouldReturnAllUsersOrderedByName() {
        List<User> users = userRepository.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void save_shouldInsertNewUser_whenIdIsNull() {
        User newUser = new User("save-new@example.com", "Save", "New", true);

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("save-new@example.com");
    }

    @Test
    void save_shouldUpdateExistingUser_whenIdIsNotNull() {
        User existingUser = userRepository.findById(1L).orElseThrow();
        existingUser.setFirstName("Updated");
        existingUser.setLastName("Name");

        User updatedUser = userRepository.save(existingUser);

        assertThat(updatedUser.getId()).isEqualTo(1L);
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("Name");
    }

    @Test
    void deleteById_shouldRemoveUser() {
        User newUser = new User("delete-me@example.com", "Delete", "Me", true);
        Long userId = userRepository.insertWithGeneratedKeys(newUser);

        userRepository.deleteById(userId);

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }
}
