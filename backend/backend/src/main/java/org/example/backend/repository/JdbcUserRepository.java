package org.example.backend.repository;

import org.example.backend.dto.response.UserAuthResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcClient jdbc;

    public JdbcUserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UserAuthResponseDto> findAuthByEmail(String email) {
        String sql = """
            select
                u.id            as id,
                u.role          as role,
                u.email         as email,
                u.password_hash as password_hash,
                u.is_active     as is_active,
                u.blocked       as blocked
            from users u
            where u.email = :email
        """;

        return jdbc.sql(sql)
                .param("email", email)
                .query(UserAuthResponseDto.class)
                .optional();
    }

    @Override
    public Optional<UserAuthResponseDto> findAuthById(Long id) {
        String sql = """
        select
            u.id            as id,
            u.role          as role,
            u.email         as email,
            u.password_hash as password_hash,
            u.is_active     as is_active,
            u.blocked       as blocked
        from users u
        where u.id = :id
    """;

        return jdbc.sql(sql)
                .param("id", id)
                .query(UserAuthResponseDto.class)
                .optional();
    }

    @Override
    public int updatePasswordHash(Long id, String newPasswordHash) {
        String sql = """
        update users
        set password_hash = :hash,
            updated_at = now()
        where id = :id
    """;

        return jdbc.sql(sql)
                .param("hash", newPasswordHash)
                .param("id", id)
                .update();
    }

}
