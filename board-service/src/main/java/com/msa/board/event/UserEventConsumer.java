package com.msa.board.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.board.entity.UserInfo;
import com.msa.board.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserInfoRepository userInfoRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-created", groupId = "board-service")
    public void handleUserCreated(String message) {
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);

            Optional<UserInfo> existing = userInfoRepository.findByUserId(event.getUserId());

            if (existing.isPresent()) {
                existing.get().update(event.getName(), event.getEmail());
                userInfoRepository.save(existing.get());
                log.info("사용자 정보 업데이트: userId={}", event.getUserId());
            } else {
                UserInfo userInfo = UserInfo.builder()
                        .userId(event.getUserId())
                        .name(event.getName())
                        .email(event.getEmail())
                        .build();
                userInfoRepository.save(userInfo);
                log.info("사용자 정보 저장: userId={}, name={}", event.getUserId(), event.getName());
            }
        } catch (Exception e) {
            log.error("user-created 이벤트 처리 실패: {}", e.getMessage());
        }
    }
}
