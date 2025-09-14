package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Nested
    class ChangeUserRole {

        @Test
        void 사용자_권한_변경에_성공한다() {
            // given
            long userId = 1L;
            UserRoleChangeRequest request = new UserRoleChangeRequest("ROLE_ADMIN");
            User user = new User("test@example.com", "password", "nickname", UserRole.ROLE_USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            userAdminService.changeUserRole(userId, request);

            // then
            assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_ADMIN);
        }

        @Test
        void 사용자가_존재하지_않으면_예외가_발생한다() {
            // given
            long userId = 1L;
            UserRoleChangeRequest request = new UserRoleChangeRequest("ROLE_ADMIN");
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                userAdminService.changeUserRole(userId, request);
            });
            assertThat(exception.getMessage()).isEqualTo("User not found");
        }
    }
}
