package org.example.expert.domain.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private ManagerService managerService;

    @Nested
    class SaveManager {

        @Test
        void 담당자_저장에_성공한다() {
            // given
            long ownerId = 1L;
            long todoId = 1L;
            long managerUserId = 2L;
            AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner",
                UserRole.ROLE_USER);
            ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);

            User owner = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", owner);
            ReflectionTestUtils.setField(todo, "id", todoId);

            User managerUser = new User("manager@test.com", "password", "manager",
                UserRole.ROLE_USER);
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            Manager manager = new Manager(managerUser, todo);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
            given(managerRepository.save(any(Manager.class))).willReturn(manager);

            // when
            ManagerSaveResponse response = managerService.saveManager(authUser, todoId, request);

            // then
            assertNotNull(response);
            assertEquals(managerUser.getId(), response.getUser().getId());
            then(todoRepository).should(times(1)).findById(todoId);
            then(userRepository).should(times(1)).findById(managerUserId);
            then(managerRepository).should(times(1)).save(any(Manager.class));
        }

        @Test
        void 존재하지_않는_Todo에_담당자를_등록하면_예외가_발생한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "owner@test.com", "owner", UserRole.ROLE_USER);
            long todoId = 1L;
            ManagerSaveRequest request = new ManagerSaveRequest(2L);

            given(todoRepository.findById(todoId)).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, request));
            then(todoRepository).should(times(1)).findById(todoId);
            then(userRepository).should(never()).findById(any());
            then(managerRepository).should(never()).save(any());
        }

        @Test
        void Todo_작성자가_아닌_유저가_담당자를_등록하면_예외가_발생한다() {
            // given
            long ownerId = 1L;
            long todoId = 1L;
            AuthUser authUser = new AuthUser(99L, "another@test.com", "another",
                UserRole.ROLE_USER);
            ManagerSaveRequest request = new ManagerSaveRequest(2L);

            User owner = new User("owner@test.com", "password", "owner", UserRole.ROLE_USER);
            ReflectionTestUtils.setField(owner, "id", ownerId);
            Todo todo = new Todo("title", "contents", "sunny", owner);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, request));
            then(todoRepository).should(times(1)).findById(todoId);
            then(userRepository).should(never()).findById(any());
            then(managerRepository).should(never()).save(any());
        }

        @Test
        void 존재하지_않는_유저를_담당자로_등록하면_예외가_발생한다() {
            // given
            long ownerId = 1L;
            long todoId = 1L;
            long managerUserId = 2L;
            AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner",
                UserRole.ROLE_USER);
            ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);

            User owner = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", owner);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, request));
            then(todoRepository).should(times(1)).findById(todoId);
            then(userRepository).should(times(1)).findById(managerUserId);
            then(managerRepository).should(never()).save(any());
        }

        @Test
        void 자신을_담당자로_등록하면_예외가_발생한다() {
            // given
            long ownerId = 1L;
            long todoId = 1L;
            AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner",
                UserRole.ROLE_USER);
            ManagerSaveRequest request = new ManagerSaveRequest(ownerId);

            User owner = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", owner);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, request));
            then(todoRepository).should(times(1)).findById(todoId);
            then(userRepository).should(times(1)).findById(ownerId);
            then(managerRepository).should(never()).save(any());
        }
    }

    @Nested
    class GetManagers {

        @Test
        void 담당자_목록_조회에_성공한다() {
            // given
            long todoId = 1L;

            User owner = new User("owner@test.com", "password", "owner", UserRole.ROLE_USER);
            Todo todo = new Todo("title", "contents", "sunny", owner);
            // 운영 코드에서 todo.getId()로
            // managerRepository.findByTodoIdWithUser() 메서드를 호출 하기 때문에
            // ReflectionTestUtils로 id 주입 필요
            ReflectionTestUtils.setField(todo, "id", todoId);

            List<User> users = List.of(
                new User("manager1@example.com", "password", "manager1", UserRole.ROLE_USER),
                new User("manager2@example.com", "password", "manager2", UserRole.ROLE_USER)
            );

            List<Manager> managers = users.stream()
                .map(user -> new Manager(user, todo))
                .toList();

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managers);

            // when
            List<ManagerResponse> responses = managerService.getManagers(todoId);

            // then
            assertFalse(responses.isEmpty());
            assertEquals(2, responses.size());
            then(todoRepository).should(times(1)).findById(todoId);
            then(managerRepository).should(times(1)).findByTodoIdWithUser(todoId);
        }

        @Test
        void 존재하지_않는_Todo의_담당자_목록을_조회하면_예외가_발생한다() {
            // given
            long todoId = 1L;
            given(todoRepository.findById(todoId)).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
            then(todoRepository).should(times(1)).findById(todoId);
            then(managerRepository).should(never()).findByTodoIdWithUser(any());
        }
    }

    @Nested
    class DeleteManager {

        @Test
        void 담당자_삭제에_성공한다() {
            // given
            long ownerId = 1L;
            long todoId = 1L;
            long managerId = 1L;
            AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner",
                UserRole.ROLE_USER);

            User owner = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", owner);

            User managerUser = new User("manager@test.com", "password", "manager",
                UserRole.ROLE_USER);
            Manager manager = new Manager(managerUser, todo);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

            // when
            managerService.deleteManager(authUser, todoId, managerId);

            // then
            then(todoRepository).should(times(1)).findById(todoId);
            then(managerRepository).should(times(1)).findById(managerId);
            then(managerRepository).should(times(1)).delete(manager);
        }

        @Test
        void 존재하지_않는_Todo에서_담당자를_삭제하면_예외가_발생한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "owner@test.com", "owner", UserRole.ROLE_USER);
            long todoId = 1L;
            long managerId = 1L;

            given(todoRepository.findById(todoId)).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, todoId, managerId));
            then(todoRepository).should(times(1)).findById(todoId);
            then(managerRepository).should(never()).findById(any());
            then(managerRepository).should(never()).delete(any());
        }

        @Test
        void Todo_작성자가_아닌_유저가_담당자를_삭제하면_예외가_발생한다() {
            // given
            long todoId = 1L;
            long managerId = 1L;
            AuthUser authUser = new AuthUser(99L, "another@test.com", "another",
                UserRole.ROLE_USER);

            User owner = new User("owner@test.com", "password", "owner", UserRole.ROLE_USER);
            Todo todo = new Todo("title", "contents", "sunny", owner);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, todoId, managerId));
            then(todoRepository).should(times(1)).findById(todoId);
            then(managerRepository).should(never()).findById(any());
            then(managerRepository).should(never()).delete(any());
        }

        @Test
        void 존재하지_않는_담당자를_삭제하면_예외가_발생한다() {
            // given
            long ownerId = 1L;
            long todoId = 1L;
            long managerId = 1L;
            AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner",
                UserRole.ROLE_USER);

            User owner = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", owner);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findById(managerId)).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, todoId, managerId));
            then(todoRepository).should(times(1)).findById(todoId);
            then(managerRepository).should(times(1)).findById(managerId);
            then(managerRepository).should(never()).delete(any());
        }

        @Test
        void 해당_일정에_등록되지_않은_담당자를_삭제하면_예외가_발생한다() {
            // given
            long ownerId = 1L;
            long todoId = 1L;
            long anotherTodoId = 2L;
            long managerId = 1L;
            AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner",
                UserRole.ROLE_USER);

            User owner = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "contents", "sunny", owner);
            ReflectionTestUtils.setField(todo, "id", todoId);

            Todo anotherTodo = new Todo("another title", "another contents", "cloudy", owner);
            ReflectionTestUtils.setField(anotherTodo, "id", anotherTodoId);

            User managerUser = new User("manager@test.com", "password", "manager",
                UserRole.ROLE_USER);
            Manager manager = new Manager(managerUser, anotherTodo);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

            // when & then
            assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(authUser, todoId, managerId));
            then(todoRepository).should(times(1)).findById(todoId);
            then(managerRepository).should(times(1)).findById(managerId);
            then(managerRepository).should(never()).delete(any());
        }
    }
}
