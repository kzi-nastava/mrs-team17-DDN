package org.example.backend.repository;

import org.example.backend.dto.request.UpdateUserProfileRequestDto;
import org.example.backend.dto.response.UserProfileResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcUserProfileRepository implements UserProfileRepository {

    private final JdbcClient jdbc;

    public JdbcUserProfileRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UserProfileResponseDto> findById(Long userId) {
        String sql = """
            select id, email, first_name, last_name, address, phone, profile_image_url, role
            from users
            where id = :id and role = 'PASSENGER'
        """;

        return jdbc.sql(sql)
                .param("id", userId)
                .query((rs, rowNum) -> {
                    UserProfileResponseDto dto = new UserProfileResponseDto();
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
    public int updateProfile(Long userId, UpdateUserProfileRequestDto req) {
        String sql = """
            update users
            set
              first_name = coalesce(:firstName, first_name),
              last_name  = coalesce(:lastName, last_name),
              address    = coalesce(:address, address),
              phone      = coalesce(:phone, phone),
              profile_image_url = coalesce(:profileImageUrl, profile_image_url)
            where id = :id and role = 'PASSENGER'
        """;

        return jdbc.sql(sql)
                .param("firstName", trimToNull(req.getFirstName()))
                .param("lastName", trimToNull(req.getLastName()))
                .param("address", trimToNull(req.getAddress()))
                .param("phone", trimToNull(req.getPhoneNumber()))
                .param("profileImageUrl", trimToNull(req.getProfileImageUrl()))
                .param("id", userId)
                .update();
    }

    @Override
    public int updateProfileImage(Long userId, String profileImageUrl) {
        String sql = """
            update users
            set profile_image_url = :url
            where id = :id and role = 'PASSENGER'
        """;

        return jdbc.sql(sql)
                .param("url", trimToNull(profileImageUrl))
                .param("id", userId)
                .update();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
