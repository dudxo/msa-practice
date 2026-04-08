package com.msa.point.dto;

import com.msa.point.entity.PointHistory;
import com.msa.point.entity.PointType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointResponse {

    private Long id;
    private Long userId;
    private int amount;
    private PointType type;
    private String description;
    private int balance;
    private LocalDateTime createdAt;

    public static PointResponse from(PointHistory history, int balance) {
        return new PointResponse(
                history.getId(),
                history.getUserId(),
                history.getAmount(),
                history.getType(),
                history.getDescription(),
                balance,
                history.getCreatedAt()
        );
    }
}
