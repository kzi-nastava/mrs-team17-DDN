package org.example.backend.repository;

import java.util.List;
import java.util.Map;

public interface UserDeviceTokenRepository {

    void upsertToken(long userId, String token, String platform);

    Map<Long, List<String>> findTokensByUserIds(List<Long> userIds);

    void deleteToken(String token);
}
