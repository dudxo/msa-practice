package com.msa.point.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.point.dto.PointRequest;
import com.msa.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardEventConsumerTest {

    @Mock
    private PointService pointService;

    private BoardEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new BoardEventConsumer(pointService, objectMapper);
    }

    // T-POINT-007
    @Test
    @DisplayName("board-created 이벤트 수신 시 10P 활동 점수가 적립된다")
    void handleBoardCreated_earns10Points() throws Exception {
        // given
        BoardCreatedEvent event = new BoardCreatedEvent(1L, 1L, "제목", LocalDateTime.now());
        String message = objectMapper.writeValueAsString(event);
        given(pointService.earn(any())).willReturn(null);

        // when
        consumer.handleBoardCreated(message);

        // then
        ArgumentCaptor<PointRequest> captor = ArgumentCaptor.forClass(PointRequest.class);
        verify(pointService).earn(captor.capture());

        PointRequest captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo(1L);
        assertThat(captured.getAmount()).isEqualTo(10);
    }
}
