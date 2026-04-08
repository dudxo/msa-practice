package com.msa.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PointType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PointHistory(Long id, Long userId, int amount, PointType type, String description) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}
