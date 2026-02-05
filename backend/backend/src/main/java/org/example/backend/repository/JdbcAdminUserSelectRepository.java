package org.example.backend.repository;

import org.example.backend.dto.response.AdminUserOptionResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcAdminUserSelectRepository implements AdminUserSelectRepository {

    private final JdbcClient jdbc;

    public JdbcAdminUserSelectRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<AdminUserOptionResponseDto> listUsersByRole(String role, String query, int limit) {
        String r = (role == null) ? "" : role.trim().toUpperCase();
        String q = (query == null) ? "" : query.trim();

        String qLike = "%" + q + "%";

        if ("DRIVER".equals(r)) {
            String sql = """
                select u.id, u.email, u.first_name, u.last_name
                from users u
                join drivers d on d.user_id = u.id
                where u.role = 'DRIVER'
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
                    .param("q", q)
                    .param("qLike", qLike)
                    .param("limit", limit)
                    .query((rs, rowNum) -> new AdminUserOptionResponseDto(
                            rs.getLong("id"),
                            rs.getString("email"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    ))
                    .list();
        }

        if ("PASSENGER".equals(r)) {
            String sql = """
                select u.id, u.email, u.first_name, u.last_name
                from users u
                where u.role = 'PASSENGER'
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
                    .param("q", q)
                    .param("qLike", qLike)
                    .param("limit", limit)
                    .query((rs, rowNum) -> new AdminUserOptionResponseDto(
                            rs.getLong("id"),
                            rs.getString("email"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    ))
                    .list();
        }

        return List.of();
    }
}
