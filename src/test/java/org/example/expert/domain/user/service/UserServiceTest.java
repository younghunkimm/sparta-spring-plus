package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    class GetUser {

        @Test
        void 사용자_단건_조회에_성공한다() {
            // given
            long userId = 1L;
            User user = new User("test@example.com", "password", "nickname", UserRole.ROLE_USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            UserResponse response = userService.getUser(userId);

            // then
            assertThat(response.getEmail()).isEqualTo(user.getEmail());
        }

        @Test
        void 사용자가_존재하지_않으면_예외가_발생한다() {
            // given
            long userId = 1L;
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                userService.getUser(userId);
            });
            assertThat(exception.getMessage()).isEqualTo("User not found");
        }
    }

    @Nested
    class ChangePassword {

        @Test
        void 비밀번호_변경에_성공한다() {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = new UserChangePasswordRequest(
                "oldPassword",
                "newPassword1A"
            );
            User user = new User(
                "test@example.com",
                "encodedOldPassword",
                "nickname",
                UserRole.ROLE_USER
            );

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
                .willReturn(false);
            given(passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
                .willReturn(true);
            given(passwordEncoder.encode(anyString()))
                .willReturn("encodedNewPassword");

            // when
            userService.changePassword(userId, request);

            // then
            assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        }

        @Test
        void 사용자가_존재하지_않으면_예외가_발생한다() {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = new UserChangePasswordRequest(
                "oldPassword",
                "newPassword1A"
            );
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                userService.changePassword(userId, request);
            });
            assertThat(exception.getMessage()).isEqualTo("User not found");
        }

        @Test
        void 새_비밀번호가_기존_비밀번호와_같으면_예외가_발생한다() {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = new UserChangePasswordRequest(
                "oldPassword",
                "newPassword1A"
            );
            User user = new User(
                "test@example.com",
                "encodedOldPassword",
                "nickname",
                UserRole.ROLE_USER
            );

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
                .willReturn(true);

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                userService.changePassword(userId, request);
            });
            assertThat(exception.getMessage()).isEqualTo("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        @Test
        void 현재_비밀번호가_일치하지_않으면_예외가_발생한다() {
            // given
            long userId = 1L;
            UserChangePasswordRequest request = new UserChangePasswordRequest(
                "oldPassword",
                "newPassword1A"
            );
            User user = new User("test@example.com",
                "encodedOldPassword",
                "nickname",
                UserRole.ROLE_USER
            );

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
                .willReturn(false);
            given(passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
                .willReturn(false);

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                userService.changePassword(userId, request);
            });
            assertThat(exception.getMessage()).isEqualTo("잘못된 비밀번호입니다.");
        }

        @Test
        void 새_비밀번호가_조건에_맞지_않으면_예외가_발생한다() {
            // given
            long userId = 1L;
            // 8자 미만
            UserChangePasswordRequest request = new UserChangePasswordRequest(
                "oldPassword",
                "new1A"
            );

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                userService.changePassword(userId, request);
            });
            assertThat(exception.getMessage()).isEqualTo("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }
}
