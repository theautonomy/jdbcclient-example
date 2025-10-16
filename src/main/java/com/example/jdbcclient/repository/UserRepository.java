package com.example.jdbcclient.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.dto.UserFilter;
import com.example.jdbcclient.dto.UserSummary;
import com.example.jdbcclient.model.User;
import com.example.jdbcclient.model.UserStatus;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // ==================================================
    // SLIDE: Basic Query Operations
    // ==================================================

    /** Slide Example: Simple Select Query */
    public List<User> findActiveUsers() {
        return jdbcClient
                .sql("SELECT * FROM users WHERE active = :active")
                .param("active", true)
                .query(User.class)
                .list();
    }

    // ==================================================
    // SLIDE: Why JdbcClient? - Modern JdbcClient example
    // ==================================================

    /** Slide Example: Modern JdbcClient with named parameters */
    public Optional<User> findByIdAndActive(Long userId) {
        return jdbcClient
                .sql("SELECT * FROM users WHERE id = :id AND active = :active")
                .param("id", userId)
                .param("active", true)
                .query(User.class)
                .optional();
    }

    // ==================================================
    // SLIDE: Parameter Binding
    // ==================================================

    /** Slide Example: Named Parameters */
    public Optional<User> findByEmailAndStatus(String email, UserStatus status) {
        return jdbcClient
                .sql("SELECT * FROM users WHERE email = :email AND active = :status")
                .param("email", email)
                .param("status", status == UserStatus.ACTIVE)
                .query(User.class)
                .optional();
    }

    /**
     * Slide Example: Parameter Source Objects NOTE: paramSource with records doesn't work properly
     * - enum and record field mapping issues The slide example shows this pattern but it has
     * limitations
     */
    public List<User> findByFilter(UserFilter filter) {
        // Workaround: Use explicit params instead of paramSource for records with enums
        return jdbcClient
                .sql(
                        "SELECT * FROM users WHERE email = :email AND active = :status AND age >= :minAge")
                .param("email", filter.email())
                .param("status", filter.status() == UserStatus.ACTIVE)
                .param("minAge", filter.minAge())
                .query(User.class)
                .list();
    }

    // ==================================================
    // SLIDE: Result Mapping
    // ==================================================

    /** Slide Example: Entity Mapping with Optional */
    public Optional<User> findById(Long id) {
        return jdbcClient
                .sql("SELECT * FROM users WHERE id = :id")
                .param("id", id)
                .query(User.class)
                .optional();
    }

    /** Slide Example: Custom Row Mapper */
    public List<UserSummary> getUserSummaries() {
        return jdbcClient
                .sql("SELECT id, email, CONCAT(first_name, ' ', last_name) as full_name FROM users")
                .query(
                        (rs, rowNum) ->
                                new UserSummary(
                                        rs.getLong("id"),
                                        rs.getString("email"),
                                        rs.getString("full_name")))
                .list();
    }

    // ==================================================
    // SLIDE: Insert Operations
    // ==================================================

    /** Slide Example: Simple Insert */
    public int insertSimple(User user) {
        return jdbcClient
                .sql(
                        "INSERT INTO users (email, first_name, last_name, active) VALUES (:email, :firstName, :lastName, :active)")
                .param("email", user.getEmail())
                .param("firstName", user.getFirstName())
                .param("lastName", user.getLastName())
                .param("active", user.isActive())
                .update();
    }

    /** Slide Example: Insert with Generated Keys */
    public Long insertWithGeneratedKeys(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient
                .sql(
                        "INSERT INTO users (email, first_name, last_name) VALUES (:email, :firstName, :lastName)")
                .paramSource(user)
                .update(keyHolder);

        // H2 returns multiple keys including timestamps, we need to get the ID specifically
        Number key = (Number) keyHolder.getKeys().get("ID");
        return key.longValue();
    }

    // ==================================================
    // SLIDE: Update and Delete
    // ==================================================

    /** Slide Example: Update Operations */
    public int updateEmail(Long userId, String newEmail) {
        return jdbcClient
                .sql(
                        "UPDATE users SET email = :email, last_modified = :lastModified WHERE id = :id")
                .param("email", newEmail)
                .param("lastModified", Instant.now())
                .param("id", userId)
                .update();
    }

    /** Slide Example: Delete Operations */
    public int deleteInactiveOldUsers(LocalDateTime cutoffDate) {
        return jdbcClient
                .sql("DELETE FROM users WHERE active = :active AND created_date < :cutoffDate")
                .param("active", false)
                .param("cutoffDate", cutoffDate)
                .update();
    }

    // ==================================================
    // SLIDE: Advanced Features
    // ==================================================

    /** Slide Example: Custom Result Set Extractors */
    public Map<String, List<User>> getUsersByDepartment() {
        return jdbcClient
                .sql("SELECT * FROM users ORDER BY department")
                .query(
                        (rs) -> {
                            Map<String, List<User>> result = new HashMap<>();
                            while (rs.next()) {
                                String dept = rs.getString("department");
                                User user = new User();
                                user.setId(rs.getLong("id"));
                                user.setEmail(rs.getString("email"));
                                user.setFirstName(rs.getString("first_name"));
                                user.setLastName(rs.getString("last_name"));
                                user.setActive(rs.getBoolean("active"));
                                user.setDepartment(dept);
                                result.computeIfAbsent(dept, k -> new ArrayList<>()).add(user);
                            }
                            return result;
                        });
    }

    /** Slide Example: Streaming Results (for testing) */
    public long countActiveUsersViaStream() {
        return jdbcClient.sql("SELECT * FROM users").query(User.class).stream()
                .filter(User::isActive)
                .count();
    }

    // ==================================================
    // SLIDE: Migration from JdbcTemplate
    // ==================================================

    /** Slide Example: After (JdbcClient) */
    public List<User> findUsersByAgeRange(int minAge, int maxAge) {
        return jdbcClient
                .sql("SELECT * FROM users WHERE age BETWEEN :minAge AND :maxAge")
                .param("minAge", minAge)
                .param("maxAge", maxAge)
                .query(User.class)
                .list();
    }

    // ==================================================
    // SLIDE: Best Practices
    // ==================================================

    private static final String FIND_BY_EMAIL =
            """
        SELECT id, email, first_name, last_name, active, created_date
        FROM users
        WHERE email = :email
        """;

    /** Slide Example: Best Practices - SQL constants */
    public Optional<User> findByEmail(String email) {
        return jdbcClient.sql(FIND_BY_EMAIL).param("email", email).query(User.class).optional();
    }

    // ==================================================
    // SLIDE: Complete Example: User Management
    // ==================================================

    public List<User> findAll() {
        return jdbcClient
                .sql("SELECT * FROM users ORDER BY last_name, first_name")
                .query(User.class)
                .list();
    }

    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient
                .sql(
                        """
            INSERT INTO users (email, first_name, last_name, active, created_date)
            VALUES (:email, :firstName, :lastName, :active, :createdDate)
            """)
                .param("email", user.getEmail())
                .param("firstName", user.getFirstName())
                .param("lastName", user.getLastName())
                .param("active", user.isActive())
                .param("createdDate", Instant.now())
                .update(keyHolder);

        Number key = (Number) keyHolder.getKeys().get("ID");
        user.setId(key.longValue());
        return user;
    }

    private User update(User user) {
        jdbcClient
                .sql(
                        """
            UPDATE users
            SET email = :email, first_name = :firstName, last_name = :lastName,
                active = :active, last_modified = :lastModified
            WHERE id = :id
            """)
                .param("email", user.getEmail())
                .param("firstName", user.getFirstName())
                .param("lastName", user.getLastName())
                .param("active", user.isActive())
                .param("lastModified", Instant.now())
                .param("id", user.getId())
                .update();

        return user;
    }

    public void deleteById(Long id) {
        jdbcClient.sql("DELETE FROM users WHERE id = :id").param("id", id).update();
    }
}
