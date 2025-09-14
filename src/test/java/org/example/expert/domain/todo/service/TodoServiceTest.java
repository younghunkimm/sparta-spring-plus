package org.example.expert.domain.todo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Nested
    class SaveTodo {

        @Test
        void todo_저장에_성공한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "test@test.com", "nickname", UserRole.ROLE_USER);
            TodoSaveRequest request = new TodoSaveRequest("title", "contents");
            User user = User.fromAuthUser(authUser);
            String weather = "Sunny";
            Todo todo = new Todo(request.getTitle(), request.getContents(), weather, user);

            given(weatherClient.getTodayWeather()).willReturn(weather);
            given(todoRepository.save(any(Todo.class))).willReturn(todo);

            // when
            TodoSaveResponse response = todoService.saveTodo(authUser, request);

            // then
            assertNotNull(response);
            assertEquals(request.getTitle(), response.getTitle());
            assertEquals(request.getContents(), response.getContents());
            assertEquals(weather, response.getWeather());
            assertEquals(user.getId(), response.getUser().getId());
            then(weatherClient).should(times(1)).getTodayWeather();
            then(todoRepository).should(times(1)).save(any(Todo.class));
        }
    }

    @Nested
    class GetTodos {

        @Test
        void todo_목록_조회에_성공한다() {
            // given
            String weather = "Sunny";
            LocalDate startDate = LocalDate.now().minusDays(1);
            LocalDate endDate = LocalDate.now();
            int page = 1;
            int size = 10;
            Pageable pageable = PageRequest.of(page - 1, size);
            User user = new User("test@test.com", "password", "nickname", UserRole.ROLE_USER);
            Todo todo = new Todo("title", "contents", weather, user);
            Page<Todo> todoPage = new PageImpl<>(List.of(todo), pageable, 1);

            given(todoRepository.searchWithUser(weather, startDate, endDate, pageable))
                .willReturn(todoPage);

            // when
            Page<TodoResponse> response =
                todoService.getTodos(weather, startDate, endDate, page, size);

            // then
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());
            assertEquals(todo.getTitle(), response.getContent().get(0).getTitle());
            then(todoRepository).should(times(1))
                .searchWithUser(weather, startDate, endDate, pageable);
        }
    }

    @Nested
    class GetTodo {

        @Test
        void todo_단건_조회에_성공한다() {
            // given
            long todoId = 1L;
            User user = new User("test@test.com", "password", "nickname", UserRole.ROLE_USER);
            Todo todo = new Todo("title", "contents", "Sunny", user);
            given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

            // when
            TodoResponse response = todoService.getTodo(todoId);

            // then
            assertNotNull(response);
            assertEquals(todo.getTitle(), response.getTitle());
            assertEquals(todo.getContents(), response.getContents());
            then(todoRepository).should(times(1)).findByIdWithUser(todoId);
        }

        @Test
        void 존재하지_않는_todo를_조회할_경우_예외가_발생한다() {
            // given
            long todoId = 1L;
            given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class, () -> todoService.getTodo(todoId));
            then(todoRepository).should(times(1)).findByIdWithUser(todoId);
        }
    }

}