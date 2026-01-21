package org.example.backend.repository;

import org.example.backend.dto.request.UpdateAdminProfileRequestDto;
import org.example.backend.dto.response.AdminProfileResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcAdminProfileRepository implements AdminProfileRepository {

    private final JdbcClient jdbc;

    public JdbcAdminProfileRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<AdminProfileResponseDto> findById(Long adminId) {
        String sql = """
            select id, email, first_name, last_name, address, phone, profile_image_url, role
            from users
            where id = :id and role = 'ADMIN'
        """;

        return jdbc.sql(sql)
                .param("id", adminId)
                .query((rs, rowNum) -> {
                    AdminProfileResponseDto dto = new AdminProfileResponseDto();
                    dto.setId(rs.getLong("id"));
                    dto.setEmail(rs.getString("email"));
                    dto.setFirstName(rs.getString("first_name"));
                    dto.setLastName(rs.getString("last_name"));
                    dto.setAddress(rs.getString("address"));
                    dto.setPhoneNumber(rs.getString("phone"));
                    dto.setProfileImageUrl(rs.getString("profile_image_url"));
                    dto.setRole(rs.getString("role"));
                    return dto;
                })
                .optional();
    }

    @Override
    public int updateProfile(Long adminId, UpdateAdminProfileRequestDto req) {
        String sql = """
            update users
            set
              first_name = coalesce(:firstName, first_name),
              last_name  = coalesce(:lastName, last_name),
              address    = coalesce(:address, address),
              phone      = coalesce(:phone, phone),
              profile_image_url = coalesce(:profileImageUrl, profile_image_url)
            where id = :id and role = 'ADMIN'
        """;

        return jdbc.sql(sql)
                .param("firstName", trimToNull(req.getFirstName()))
                .param("lastName", trimToNull(req.getLastName()))
                .param("address", trimToNull(req.getAddress()))
                .param("phone", trimToNull(req.getPhoneNumber()))
                .param("profileImageUrl", trimToNull(req.getProfileImageUrl()))
                .param("id", adminId)
                .update();
    }

    @Override
    public int updateProfileImage(Long adminId, String profileImageUrl) {
        String sql = """
            update users
            set profile_image_url = :url
            where id = :id and role = 'ADMIN'
        """;

        return jdbc.sql(sql)
                .param("url", trimToNull(profileImageUrl))
                .param("id", adminId)
                .update();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
