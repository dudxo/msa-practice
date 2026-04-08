package com.msa.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int balance;

    public PointBalance(Long id, Long userId, int balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    public static PointBalance createForUser(Long userId) {
        PointBalance pb = new PointBalance();
        pb.userId = userId;
        pb.balance = 0;
        return pb;
    }

    public void earn(int amount) {
        this.balance += amount;
    }

    public void deduct(int amount) {
        if (this.balance < amount) {
            throw new IllegalStateException(
                    "잔액이 부족합니다. 현재 잔액: " + this.balance + ", 차감 요청: " + amount);
        }
        this.balance -= amount;
    }
}
