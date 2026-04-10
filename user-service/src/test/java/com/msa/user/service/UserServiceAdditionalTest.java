package com.msa.user.service;

import com.msa.user.dto.UserResponse;
import com.msa.user.entity.User;
import com.msa.user.repository.UserRepository;
import com.msa.user.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceAdditionalTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // T-USER-007
    @Test
    @DisplayName("존재하는 ID 목록으로 다건 조회하면 UserResponse 리스트가 반환된다")
    void getUsersByIds_success() {
        // given
        List<Long> ids = List.of(1L, 2L);
        List<User> users = List.of(
                User.builder().id(1L).name("홍길동").email("hong@test.com").build(),
                User.builder().id(2L).name("김철수").email("kim@test.com").build()
        );
        given(userRepository.findAllById(ids)).willReturn(users);

        // when
        List<UserResponse> responses = userService.getUsersByIds(ids);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("홍길동");
        assertThat(responses.get(1).getName()).isEqualTo("김철수");
    }

    // T-USER-008
    @Test
    @DisplayName("빈 리스트로 다건 조회하면 빈 리스트가 반환된다")
    void getUsersByIds_emptyList_returnsEmpty() {
        // given
        given(userRepository.findAllById(List.of())).willReturn(List.of());

        // when
        List<UserResponse> responses = userService.getUsersByIds(List.of());

        // then
        assertThat(responses).isEmpty();
    }
}
