package com.msa.board.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishBoardCreated(BoardCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("board-created", payload);
            log.info("게시글 작성 이벤트 발행: boardId={}, authorId={}", event.getBoardId(), event.getAuthorId());
        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: {}", e.getMessage());
        }
    }
}
