package org.example.backend.repository;

import org.example.backend.dto.response.AdminUserStatusResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAdminUserManagementRepository implements AdminUserManagementRepository {

    private final JdbcClient jdbc;

    public JdbcAdminUserManagementRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<AdminUserStatusResponseDto> listUsersWithStatus(String role, String query, int limit) {
        String r = (role == null) ? "" : role.trim().toUpperCase();
        String q = (query == null) ? "" : query.trim();
        String qLike = "%" + q + "%";

        String sql = """
            select u.id, u.email, u.first_name, u.last_name, u.role, u.blocked, u.block_reason
            from users u
            where u.role = :role
              and (
                   :q = ''
                   or lower(u.email) like lower(:qLike)
                   or lower(u.first_name) like lower(:qLike)
                   or lower(u.last_name) like lower(:qLike)
              )
            order by u.last_name, u.first_name, u.id
            limit :limit
        """;

        return jdbc.sql(sql)
                .param("role", r)
                .param("q", q)
                .param("qLike", qLike)
                .param("limit", limit)
                .query((rs, rowNum) -> new AdminUserStatusResponseDto(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getBoolean("blocked"),
                        rs.getString("block_reason")
                ))
                .list();
    }

    @Override
    public int updateBlockStatus(long userId, boolean blocked, String blockReason) {
        String sql = """
            update users
            set blocked = :blocked,
                block_reason = :blockReason,
                updated_at = now()
            where id = :id
        """;

        return jdbc.sql(sql)
                .param("blocked", blocked)
                .param("blockReason", blockReason)
                .param("id", userId)
                .update();
    }

    @Override
    public Optional<AdminUserStatusResponseDto> findUserStatusById(long userId) {
        String sql = """
            select u.id, u.email, u.first_name, u.last_name, u.role, u.blocked, u.block_reason
            from users u
            where u.id = :id
        """;

        return jdbc.sql(sql)
                .param("id", userId)
                .query((rs, rowNum) -> new AdminUserStatusResponseDto(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getBoolean("blocked"),
                        rs.getString("block_reason")
                ))
                .optional();
    }
}
