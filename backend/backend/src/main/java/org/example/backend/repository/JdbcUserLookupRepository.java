package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcUserLookupRepository implements UserLookupRepository {

    private final JdbcClient jdbc;

    public JdbcUserLookupRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UserBasic> findById(Long id) {
        return jdbc.sql("""
            select id, email, first_name, last_name, is_active
            from users
            where id = :id
        """)
                .param("id", id)
                .query((rs, rowNum) -> new UserBasic(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getBoolean("is_active")
                ))
                .optional();
    }

    @Override
    public Optional<UserBasic> findByEmail(String email) {
        return jdbc.sql("""
            select id, email, first_name, last_name, is_active
            from users
            where lower(email) = lower(:email)
        """)
                .param("email", email)
                .query((rs, rowNum) -> new UserBasic(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getBoolean("is_active")
                ))
                .optional();
    }
}
