package com.msa.point.controller;

import com.msa.point.dto.BalanceResponse;
import com.msa.point.dto.PointRequest;
import com.msa.point.dto.PointResponse;
import com.msa.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/earn")
    @ResponseStatus(HttpStatus.CREATED)
    public PointResponse earn(@RequestBody @Valid PointRequest request) {
        return pointService.earn(request);
    }

    @PostMapping("/deduct")
    @ResponseStatus(HttpStatus.CREATED)
    public PointResponse deduct(@RequestBody @Valid PointRequest request) {
        return pointService.deduct(request);
    }

    @PostMapping("/refund")
    @ResponseStatus(HttpStatus.CREATED)
    public PointResponse refund(@RequestBody @Valid PointRequest request) {
        return pointService.refund(request);
    }

    @GetMapping("/balance/{userId}")
    public BalanceResponse getBalance(@PathVariable Long userId) {
        return new BalanceResponse(userId, pointService.getBalance(userId));
    }
}
