package com.msa.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BalanceResponse {

    private Long userId;
    private int balance;
}
