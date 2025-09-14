package org.example.expert.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.WithMockAuthUser;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.example.expert.support.ControllerTestSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAdminService userAdminService;

    @Nested
    class ChangeUserRole {

        private static final String BASE_URL = "/admin/users/{userId}";

        @Test
        @WithMockAuthUser(userId = 1L, email = "admin@test.com", nickname = "admin", role = UserRole.ROLE_ADMIN)
        void 사용자_권한_변경에_성공한다() throws Exception {
            // given
            long targetUserId = 2L;
            UserRoleChangeRequest request = new UserRoleChangeRequest(UserRole.ROLE_USER.name());
            willDoNothing().given(userAdminService)
                .changeUserRole(anyLong(), any(UserRoleChangeRequest.class));

            // when & then
            mockMvc.perform(
                    patch(BASE_URL, targetUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

            // void를 반환하기 때문에 행위 검증을 추가
            // - eq: 값이 같은지 비교 (`equals()` 메서드를 이용해 비교)
            // - refEq: 필드 값이 같은지 비교 (리플렉션을 이용해 필드 값을 직접 비교)
            verify(userAdminService).changeUserRole(eq(targetUserId), refEq(request));
        }
    }
}
