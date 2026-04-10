package com.msa.user.service;

import com.msa.user.dto.LoginRequest;
import com.msa.user.dto.LoginResponse;
import com.msa.user.entity.User;
import com.msa.user.event.UserEventProducer;
import com.msa.user.repository.UserRepository;
import com.msa.user.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("올바른 이메일/비밀번호로 로그인하면 JWT 토큰이 반환된다")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("hong@test.com", "password123");
        User user = User.builder()
                .id(1L).name("홍길동").email("hong@test.com").password("password123")
                .build();

        given(userRepository.findByEmail("hong@test.com")).willReturn(Optional.of(user));
        given(jwtUtil.generateToken(1L)).willReturn("mock-jwt-token");

        // when
        LoginResponse response = userService.login(request);

        // then
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
    void login_emailNotFound_throwsException() {
        // given
        LoginRequest request = new LoginRequest("wrong@test.com", "password123");
        given(userRepository.findByEmail("wrong@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 예외가 발생한다")
    void login_wrongPassword_throwsException() {
        // given
        LoginRequest request = new LoginRequest("hong@test.com", "wrongpw");
        User user = User.builder()
                .id(1L).name("홍길동").email("hong@test.com").password("password123")
                .build();

        given(userRepository.findByEmail("hong@test.com")).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }
}
