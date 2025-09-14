package org.example.expert.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.WithMockAuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.example.expert.support.ControllerTestSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Nested
    class GetUser {

        private static final String BASE_URL = "/users/{userId}";

        @Test
        @WithMockAuthUser(userId = 1L, email = "test@example.com", nickname = "test", role = UserRole.ROLE_USER)
        void 사용자_단건_조회에_성공한다() throws Exception {
            // given
            long userId = 1L;
            UserResponse response = new UserResponse(userId, "test@example.com");
            given(userService.getUser(anyLong())).willReturn(response);

            // when & then
            mockMvc.perform(get(BASE_URL, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
        }
    }

    @Nested
    class ChangePassword {

        private static final String BASE_URL = "/users";

        @Test
        @WithMockAuthUser(userId = 1L, email = "test@example.com", nickname = "test", role = UserRole.ROLE_USER)
        void 비밀번호_변경에_성공한다() throws Exception {
            // given
            UserChangePasswordRequest request = new UserChangePasswordRequest(
                "oldPassword",
                "newPassword1A"
            );
            willDoNothing().given(userService)
                .changePassword(anyLong(), any(UserChangePasswordRequest.class));

            // when & then
            mockMvc.perform(
                    put(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
        }
    }
}
