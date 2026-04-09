package com.msa.user.service;

import com.msa.user.client.PointServiceClient;
import com.msa.user.dto.CreateUserRequest;
import com.msa.user.dto.UserResponse;
import com.msa.user.entity.User;
import com.msa.user.event.UserEventProducer;
import com.msa.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServicePointTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointServiceClient pointServiceClient;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    // T-USER-009
    @Test
    @DisplayName("회원가입 후 가입 축하 포인트 적립이 호출된다")
    void createUser_callsPointService() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", "hong@test.com");
        User savedUser = User.builder().id(1L).name("홍길동").email("hong@test.com").build();
        given(userRepository.existsByEmail("hong@test.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        willDoNothing().given(pointServiceClient).earnPoints(1L, 500, "가입 축하 포인트");

        // when
        userService.createUser(request);

        // then
        verify(pointServiceClient).earnPoints(1L, 500, "가입 축하 포인트");
    }
}
