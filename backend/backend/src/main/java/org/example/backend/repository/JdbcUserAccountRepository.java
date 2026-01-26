package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserAccountRepository implements UserAccountRepository {

    private final JdbcClient jdbc;

    public JdbcUserAccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer x = jdbc.sql("select 1 from users where email = :email")
                .param("email", email)
                .query(Integer.class)
                .optional()
                .orElse(null);
        return x != null;
    }

    @Override
    public Long insertDriverUserReturningId(
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String address,
            String phone
    ) {
        String sql = """
            insert into users (role, email, password_hash, first_name, last_name, address, phone, is_active)
            values ('DRIVER', :email, :passwordHash, :firstName, :lastName, :address, :phone, false)
            returning id
        """;

        return jdbc.sql(sql)
                .param("email", email)
                .param("passwordHash", passwordHash)
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("address", address)
                .param("phone", phone)
                .query(Long.class)
                .single();
    }

    // ✅ DODATO: Passenger insert
    @Override
    public Long insertPassengerUserReturningId(
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String address,
            String phone
    ) {
        String sql = """
            insert into users (role, email, password_hash, first_name, last_name, address, phone, is_active)
            values ('PASSENGER', :email, :passwordHash, :firstName, :lastName, :address, :phone, false)
            returning id
        """;

        return jdbc.sql(sql)
                .param("email", email)
                .param("passwordHash", passwordHash)
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("address", address)
                .param("phone", phone)
                .query(Long.class)
                .single();
    }

    @Override
    public int activateAndSetPassword(Long userId, String newPasswordHash) {
        String sql = """
            update users
            set password_hash = :passwordHash,
                is_active = true,
                updated_at = now()
            where id = :userId
        """;

        return jdbc.sql(sql)
                .param("passwordHash", newPasswordHash)
                .param("userId", userId)
                .update();
    }

    
    @Override
    public int activateUser(Long userId) {
        String sql = """
            update users
            set is_active = true,
                updated_at = now()
            where id = :userId
        """;

        return jdbc.sql(sql)
                .param("userId", userId)
                .update();
    }
}