package org.example.expert.domain.user.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserUpdateProfileImageRequest;
import org.example.expert.domain.user.dto.response.UserGetProfileImageResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PatchMapping("/users/profile")
    public void updateProfileImage(
        @AuthenticationPrincipal AuthUser authUser,
        @RequestBody UserUpdateProfileImageRequest request
    ) {

        userService.updateProfileImage(authUser.getId(), request.getFileKey());
    }

    @GetMapping("/users/profile")
    public ResponseEntity<UserGetProfileImageResponse> getProfileImage(
        @AuthenticationPrincipal AuthUser authUser
    ) {

        return ResponseEntity.ok(userService.getProfileImage(authUser.getId()));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser,
        @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
        @RequestParam String nickname
    ) {

        return ResponseEntity.ok(userService.searchUsers(nickname));
    }
}
