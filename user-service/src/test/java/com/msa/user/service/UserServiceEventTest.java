package com.msa.user.service;

import com.msa.user.client.PointServiceClient;
import com.msa.user.dto.CreateUserRequest;
import com.msa.user.entity.User;
import com.msa.user.event.UserEventProducer;
import com.msa.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceEventTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointServiceClient pointServiceClient;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 시 user-created 이벤트가 발행된다")
    void createUser_publishesUserCreatedEvent() {
        // given
        CreateUserRequest request = new CreateUserRequest("테스트", "test@test.com");

        User savedUser = User.builder()
                .id(1L)
                .name("테스트")
                .email("test@test.com")
                .build();

        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        userService.createUser(request);

        // then — 이벤트 발행 검증
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);

        verify(userEventProducer).publishUserCreated(
                idCaptor.capture(), nameCaptor.capture(), emailCaptor.capture()
        );

        assertThat(idCaptor.getValue()).isEqualTo(1L);
        assertThat(nameCaptor.getValue()).isEqualTo("테스트");
        assertThat(emailCaptor.getValue()).isEqualTo("test@test.com");
    }
}
