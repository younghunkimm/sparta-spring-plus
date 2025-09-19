package org.example.expert.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserGetProfileImageResponse {

    private final String profileImageUrl;

    public UserGetProfileImageResponse(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
