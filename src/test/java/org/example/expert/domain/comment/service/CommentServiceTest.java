package org.example.expert.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Nested
    class SaveComment {

        @Test
        void 댓글_저장에_성공한다() {
            // given
            AuthUser authUser = new AuthUser(
                1L,
                "test@example.com",
                "test",
                UserRole.ROLE_USER
            );
            long todoId = 1L;
            CommentSaveRequest request = new CommentSaveRequest("댓글 내용");
            User user = User.fromAuthUser(authUser);
            Todo todo = new Todo(1L, "제목", "내용", "Sunny", user);

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(commentRepository.save(any(Comment.class))).willAnswer(
                invocation -> invocation.getArgument(0)
            );

            // when
            CommentSaveResponse response = commentService.saveComment(authUser, todoId, request);

            // then
            assertThat(response.getContents()).isEqualTo(request.getContents());
            assertThat(response.getUser().getId()).isEqualTo(authUser.getId());
        }

        @Test
        void Todo가_존재하지_않으면_예외가_발생한다() {
            // given
            AuthUser authUser = new AuthUser(
                1L,
                "test@example.com",
                "test",
                UserRole.ROLE_USER
            );
            long todoId = 1L;
            CommentSaveRequest request = new CommentSaveRequest("댓글 내용");
            given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                commentService.saveComment(authUser, todoId, request);
            });
            assertThat(exception.getMessage()).isEqualTo("Todo not found");
        }
    }

    @Nested
    class GetComments {

        @Test
        void 댓글_조회에_성공한다() {
            // given
            long todoId = 1L;
            User user = new User("test@example.com", "password", "nickname", UserRole.ROLE_USER);
            Todo todo = new Todo(1L, "제목", "내용", "Sunny", user);
            List<Comment> comments = List.of(
                new Comment("댓글1", user, todo),
                new Comment("댓글2", user, todo)
            );
            given(commentRepository.findByTodoIdWithUser(anyLong())).willReturn(comments);

            // when
            List<CommentResponse> responses = commentService.getComments(todoId);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getContents()).isEqualTo("댓글1");
            assertThat(responses.get(1).getContents()).isEqualTo("댓글2");
        }
    }
}