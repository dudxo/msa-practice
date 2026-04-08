package com.msa.user.service;

import com.msa.user.client.PointServiceClient;
import com.msa.user.dto.CreateUserRequest;
import com.msa.user.dto.UserResponse;
import com.msa.user.entity.User;
import com.msa.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointServiceClient pointServiceClient;

    @InjectMocks
    private UserService userService;

    // T-USER-001
    @Test
    @DisplayName("정상 요청으로 회원가입하면 UserResponse가 반환된다")
    void createUser_success() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", "hong@test.com");
        User savedUser = User.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@test.com")
                .build();
        given(userRepository.existsByEmail("hong@test.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserResponse response = userService.createUser(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("hong@test.com");
    }

    // T-USER-002
    @Test
    @DisplayName("이메일이 중복이면 예외가 발생한다")
    void createUser_duplicateEmail_throwsException() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", "hong@test.com");
        given(userRepository.existsByEmail("hong@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 등록된 이메일");
    }

    // T-USER-003
    @Test
    @DisplayName("존재하는 ID로 조회하면 UserResponse가 반환된다")
    void getUser_success() {
        // given
        User user = User.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@test.com")
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("홍길동");
    }

    // T-USER-004
    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
    void getUser_notFound_throwsException() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
