package org.example.expert.domain.user.dto.response;

import java.time.LocalDateTime;
import org.example.expert.domain.user.entity.User;

public record UserSearchResponse(
    Long id,
    String email,
    String nickname,
    String profileImageUrl,
    LocalDateTime createdAt
) {

    public static UserSearchResponse from(User user) {

        return new UserSearchResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            user.getCreatedAt()
        );
    }
}
