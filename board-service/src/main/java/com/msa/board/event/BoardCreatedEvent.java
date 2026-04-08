package com.msa.board.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreatedEvent {

    private Long boardId;
    private Long authorId;
    private String title;
    private LocalDateTime createdAt;
}
