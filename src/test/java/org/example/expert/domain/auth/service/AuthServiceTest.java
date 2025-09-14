package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import java.util.Optional;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
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
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    class Signup {

        @Test
        void 회원가입에_성공한다() {
            // given
            SignupRequest request = new SignupRequest("test@test.com", "password", "nickname",
                "ROLE_USER");
            User user = new User(request.getEmail(), "encodedPassword", request.getNickname(),
                UserRole.ROLE_USER);
            String expectedToken = "test-token";

            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(user);
            given(jwtUtil.createToken(any(), any(), any(), any())).willReturn(expectedToken);

            // when
            SignupResponse response = authService.signup(request);

            // then
            assertNotNull(response);
            assertEquals(expectedToken, response.getBearerToken());
            then(userRepository).should(times(1)).existsByEmail(request.getEmail());
            then(passwordEncoder).should(times(1)).encode(request.getPassword());
            then(userRepository).should(times(1)).save(any(User.class));
            then(jwtUtil).should(times(1)).createToken(any(), any(), any(), any());
        }

        @Test
        void 이미_존재하는_이메일로_회원가입에_실패한다() {
            // given
            SignupRequest request = new SignupRequest("test@test.com", "password", "nickname",
                "ROLE_USER");
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            // when & then
            assertThrows(InvalidRequestException.class, () -> authService.signup(request));
            then(userRepository).should(times(1)).existsByEmail(request.getEmail());
            then(passwordEncoder).should(never()).encode(any());
            then(userRepository).should(never()).save(any());
            then(jwtUtil).should(never()).createToken(any(), any(), any(), any());
        }
    }

    @Nested
    class Signin {

        @Test
        void 로그인에_성공한다() {
            // given
            SigninRequest request = new SigninRequest("test@test.com", "password");
            User user = new User(request.getEmail(), "encodedPassword", "nickname",
                UserRole.ROLE_USER);
            String expectedToken = "test-token";

            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(
                true);
            given(jwtUtil.createToken(any(), any(), any(), any())).willReturn(expectedToken);

            // when
            SigninResponse response = authService.signin(request);

            // then
            assertNotNull(response);
            assertEquals(expectedToken, response.getBearerToken());
            then(userRepository).should(times(1)).findByEmail(request.getEmail());
            then(passwordEncoder).should(times(1))
                .matches(request.getPassword(), user.getPassword());
            then(jwtUtil).should(times(1)).createToken(any(), any(), any(), any());
        }

        @Test
        void 가입되지_않은_유저로_로그인에_실패한다() {
            // given
            SigninRequest request = new SigninRequest("test@test.com", "password");
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class, () -> authService.signin(request));
            then(userRepository).should(times(1)).findByEmail(request.getEmail());
            then(passwordEncoder).should(never()).matches(any(), any());
            then(jwtUtil).should(never()).createToken(any(), any(), any(), any());
        }

        @Test
        void 잘못된_비밀번호로_로그인에_실패한다() {
            // given
            SigninRequest request = new SigninRequest("test@test.com", "password");
            User user = new User(request.getEmail(), "encodedPassword", "nickname",
                UserRole.ROLE_USER);

            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(
                false);

            // when & then
            assertThrows(AuthException.class, () -> authService.signin(request));
            then(userRepository).should(times(1)).findByEmail(request.getEmail());
            then(passwordEncoder).should(times(1))
                .matches(request.getPassword(), user.getPassword());
            then(jwtUtil).should(never()).createToken(any(), any(), any(), any());
        }
    }

}
