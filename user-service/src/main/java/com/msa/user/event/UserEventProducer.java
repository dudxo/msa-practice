package com.msa.user.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishUserCreated(Long userId, String name, String email) {
        try {
            UserCreatedEvent event = new UserCreatedEvent(userId, name, email);
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-created", payload);
            log.info("회원가입 이벤트 발행: userId={}, name={}", userId, name);
        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: {}", e.getMessage());
        }
    }
}
