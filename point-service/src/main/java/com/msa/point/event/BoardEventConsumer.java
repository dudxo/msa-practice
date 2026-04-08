package com.msa.point.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.point.dto.PointRequest;
import com.msa.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardEventConsumer {

    private final PointService pointService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "board-created", groupId = "point-service")
    public void handleBoardCreated(String message) {
        try {
            BoardCreatedEvent event = objectMapper.readValue(message, BoardCreatedEvent.class);
            log.info("게시글 작성 이벤트 수신: boardId={}, authorId={}", event.getBoardId(), event.getAuthorId());

            pointService.earn(new PointRequest(
                    event.getAuthorId(),
                    10,
                    "게시글 작성 활동 점수 (boardId=" + event.getBoardId() + ")"
            ));

            log.info("활동 점수 10P 적립 완료: authorId={}", event.getAuthorId());
        } catch (JsonProcessingException e) {
            log.error("이벤트 역직렬화 실패: {}", e.getMessage());
        }
    }
}
