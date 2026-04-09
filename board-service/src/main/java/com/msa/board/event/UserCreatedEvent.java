package com.msa.board.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private Long userId;
    private String name;
    private String email;
}
