package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.dto.request.UserBulkInsertRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/admin/users/{userId}")
    public void changeUserRole(@PathVariable long userId,
        @RequestBody UserRoleChangeRequest userRoleChangeRequest) {
        userAdminService.changeUserRole(userId, userRoleChangeRequest);
    }

    // User bulk insert endpoint
    // 데이터 생성 시 닉네임은 랜덤으로 지정
    @Secured(UserRole.Authority.ADMIN)
    @PostMapping("/admin/users/bulk-insert")
    public ResponseEntity<String> bulkInsertUsers(
        @RequestBody UserBulkInsertRequest userBulkInsertRequest
    ) {

        // 서비스 메서드 비동기 호출
        userAdminService.bulkInsertUsersAsync(userBulkInsertRequest.getCount());

        // 즉시 응답 반환
        return ResponseEntity.ok(userBulkInsertRequest.getCount() + "명의 사용자 대량 생성 시작");
    }
}
