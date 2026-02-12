package org.example.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.example.backend.testsupport.PostgresTestContainerBase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(
        scripts = {
                "classpath:sql/order/schema.sql",
                "classpath:sql/order/reset.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class JdbcUserLookupRepositoryOrderTest extends PostgresTestContainerBase {

    private static final long USER_ID = 200L;

    @Autowired
    private JdbcUserLookupRepository repository;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void findById_shouldReturnUserBasic_whenUserExists() {
        insertUser(USER_ID, "pera@test.com", "Pera", "Peric", "PASSENGER", true, false, null);

        Optional<UserLookupRepository.UserBasic> u = repository.findById(USER_ID);

        assertTrue(u.isPresent());
        assertEquals(USER_ID, u.get().id());
        assertEquals("pera@test.com", u.get().email());
        assertEquals("Pera", u.get().firstName());
        assertEquals("Peric", u.get().lastName());
        assertTrue(u.get().active());
        assertFalse(u.get().blocked());
        assertNull(u.get().blockReason());
    }

    @Test
    void findById_shouldReturnEmpty_whenUserDoesNotExist() {
        assertTrue(repository.findById(USER_ID).isEmpty());
    }

    @Test
    void findByEmail_shouldBeCaseInsensitive() {
        insertUser(USER_ID, "MixedCase@TEST.com", "M", "C", "DRIVER", true, false, null);

        Optional<UserLookupRepository.UserBasic> u = repository.findByEmail("mixedcase@test.COM");

        assertTrue(u.isPresent());
        assertEquals(USER_ID, u.get().id());
        assertEquals("MixedCase@TEST.com", u.get().email());
    }

    @Test
    void findRoleById_shouldReturnRole_whenUserExists() {
        insertUser(USER_ID, "r@test.com", "R", "R", "ADMIN", true, false, null);

        Optional<String> role = repository.findRoleById(USER_ID);

        assertTrue(role.isPresent());
        assertEquals("ADMIN", role.get());
    }

    @Test
    void findRoleById_shouldReturnEmpty_whenUserDoesNotExist() {
        assertTrue(repository.findRoleById(USER_ID).isEmpty());
    }

    private void insertUser(
            long id,
            String email,
            String firstName,
            String lastName,
            String role,
            boolean active,
            boolean blocked,
            String reason
    ) {
        jdbc.sql("""
                insert into users (id, email, first_name, last_name, role, is_active, blocked, block_reason)
                values (:id, :email, :fn, :ln, :role, :active, :blocked, :reason)
                """)
                .param("id", id)
                .param("email", email)
                .param("fn", firstName)
                .param("ln", lastName)
                .param("role", role)
                .param("active", active)
                .param("blocked", blocked)
                .param("reason", reason)
                .update();
    }
}
