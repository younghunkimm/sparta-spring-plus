package org.example.expert.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.expert.config.WithMockAuthUser;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.ControllerTestSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
class CommentControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Nested
    class SaveComment {

        private static final String BASE_URL = "/todos/{todoId}/comments";

        @Test
        @WithMockAuthUser(userId = 1L, email = "test@example.com", nickname = "test", role = UserRole.ROLE_USER)
        void 댓글_저장에_성공한다() throws Exception {
            // given
            long todoId = 1L;
            CommentSaveRequest request = new CommentSaveRequest("댓글 내용");
            UserResponse userResponse = new UserResponse(1L, "test@example.com");
            CommentSaveResponse response = new CommentSaveResponse(
                1L,
                request.getContents(),
                userResponse
            );

            given(commentService.saveComment(any(), anyLong(), any(CommentSaveRequest.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                    post(BASE_URL, todoId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.contents").value(response.getContents()))
                .andExpect(jsonPath("$.user.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.user.email").value(userResponse.getEmail()));
        }
    }

    @Nested
    class GetComments {

        private static final String BASE_URL = "/todos/{todoId}/comments";

        @Test
        @WithMockAuthUser(userId = 1L, email = "test@example.com", nickname = "test", role = UserRole.ROLE_USER)
        void 댓글_조회에_성공한다() throws Exception {
            // given
            long todoId = 1L;
            UserResponse userResponse = new UserResponse(1L, "test@example.com");
            List<CommentResponse> response = List.of(
                new CommentResponse(1L, "댓글 내용1", userResponse),
                new CommentResponse(2L, "댓글 내용2", userResponse)
            );
            given(commentService.getComments(anyLong())).willReturn(response);

            // when & then
            mockMvc.perform(get(BASE_URL, todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(response.size()));
        }
    }
}