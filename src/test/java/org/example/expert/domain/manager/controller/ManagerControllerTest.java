package org.example.expert.domain.manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.expert.config.WithMockAuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.ControllerTestSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManagerService managerService;

    @Nested
    class SaveManager {

        private static final String BASE_URL = "/todos/{todoId}/managers";

        @Test
        @WithMockAuthUser(userId = 1L, email = "test@example.com", nickname = "test", role = UserRole.ROLE_USER)
        void 담당자_저장에_성공한다() throws Exception {
            // given
            long todoId = 1L;
            long managerUserId = 2L;
            ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);
            UserResponse userResponse = new UserResponse(managerUserId, "manager@example.com");
            ManagerSaveResponse response = new ManagerSaveResponse(1L, userResponse);

            given(managerService.saveManager(any(), anyLong(), any(ManagerSaveRequest.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                    post(BASE_URL, todoId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.user.id").value(userResponse.getId()));
        }
    }

    @Nested
    class GetManagers {

        private static final String BASE_URL = "/todos/{todoId}/managers";

        @Test
        @WithMockAuthUser(userId = 1L, email = "test@example.com", nickname = "test", role = UserRole.ROLE_USER)
        void 담당자_조회에_성공한다() throws Exception {
            // given
            long todoId = 1L;
            UserResponse userResponse1 = new UserResponse(2L, "manager1@example.com");
            UserResponse userResponse2 = new UserResponse(3L, "manager2@example.com");
            List<ManagerResponse> response = List.of(
                new ManagerResponse(1L, userResponse1),
                new ManagerResponse(2L, userResponse2)
            );
            given(managerService.getManagers(anyLong())).willReturn(response);

            // when & then
            mockMvc.perform(get(BASE_URL, todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(response.size()));
        }
    }

    @Nested
    class DeleteManager {

        private static final String BASE_URL = "/todos/{todoId}/managers/{managerId}";

        @Test
        @WithMockAuthUser(userId = 1L, email = "test@example.com", nickname = "test", role = UserRole.ROLE_USER)
        void 담당자_삭제에_성공한다() throws Exception {
            // given
            long todoId = 1L;
            long managerId = 1L;
            willDoNothing().given(managerService).deleteManager(any(), anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete(BASE_URL, todoId, managerId))
                .andExpect(status().isOk());
        }
    }
}
