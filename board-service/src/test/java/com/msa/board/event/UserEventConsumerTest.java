package com.msa.board.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.board.entity.UserInfo;
import com.msa.board.repository.UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock
    private UserInfoRepository userInfoRepository;

    private UserEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new UserEventConsumer(userInfoRepository, objectMapper);
    }

    @Test
    @DisplayName("user-created 이벤트 수신 시 UserInfo가 로컬 DB에 저장된다")
    void handleUserCreated_savesUserInfo() throws Exception {
        // given
        String message = objectMapper.writeValueAsString(
                new UserCreatedEvent(1L, "홍길동", "hong@test.com")
        );
        given(userInfoRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userInfoRepository.save(any(UserInfo.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        consumer.handleUserCreated(message);

        // then
        ArgumentCaptor<UserInfo> captor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userInfoRepository).save(captor.capture());

        UserInfo saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("이미 존재하는 유저 정보가 오면 업데이트한다")
    void handleUserCreated_updatesExistingUserInfo() throws Exception {
        // given
        UserInfo existing = UserInfo.builder()
                .userId(1L)
                .name("기존이름")
                .email("old@test.com")
                .build();

        String message = objectMapper.writeValueAsString(
                new UserCreatedEvent(1L, "새이름", "new@test.com")
        );
        given(userInfoRepository.findByUserId(1L)).willReturn(Optional.of(existing));
        given(userInfoRepository.save(any(UserInfo.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        consumer.handleUserCreated(message);

        // then
        ArgumentCaptor<UserInfo> captor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userInfoRepository).save(captor.capture());

        UserInfo updated = captor.getValue();
        assertThat(updated.getName()).isEqualTo("새이름");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }
}
