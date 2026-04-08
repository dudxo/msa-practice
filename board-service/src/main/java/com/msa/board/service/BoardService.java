package com.msa.board.service;

import com.msa.board.client.PointServiceClient;
import com.msa.board.client.UserServiceClient;
import com.msa.board.dto.CreateBoardRequest;
import com.msa.board.event.BoardCreatedEvent;
import com.msa.board.event.BoardEventProducer;
import com.msa.board.dto.BoardResponse;
import com.msa.board.entity.Board;
import com.msa.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserServiceClient userServiceClient;
    private final PointServiceClient pointServiceClient;
    private final BoardEventProducer boardEventProducer;

    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request) {
        // 1. 포인트 차감 (실패 시 게시글 생성 안 함)
        pointServiceClient.deductPoints(request.getAuthorId(), 100, "게시글 작성");

        try {
            // 2. 게시글 생성
            Board board = Board.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .authorId(request.getAuthorId())
                    .build();
            Board savedBoard = boardRepository.save(board);

            // 3. 활동 점수 적립 — Kafka 이벤트로 비동기 처리
            boardEventProducer.publishBoardCreated(new BoardCreatedEvent(
                    savedBoard.getId(),
                    savedBoard.getAuthorId(),
                    savedBoard.getTitle(),
                    savedBoard.getCreatedAt()
            ));

            String authorName = userServiceClient.getUserName(savedBoard.getAuthorId());
            return BoardResponse.from(savedBoard, authorName);
        } catch (Exception e) {
            // Saga 보상 트랜잭션: 차감한 포인트 환불
            pointServiceClient.refundPoints(request.getAuthorId(), 100, "게시글 작성 실패 환불");
            throw e;
        }
    }

    public BoardResponse getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + id));

        String authorName = userServiceClient.getUserName(board.getAuthorId());
        return BoardResponse.from(board, authorName);
    }

    public List<BoardResponse> getAllBoards() {
        List<Board> boards = boardRepository.findAll();

        // Batch API로 N+1 방지: authorId 목록을 한 번에 조회
        List<Long> authorIds = boards.stream()
                .map(Board::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> authorNames = userServiceClient.getUserNamesByIds(authorIds);

        return boards.stream()
                .map(board -> BoardResponse.from(
                        board,
                        authorNames.getOrDefault(board.getAuthorId(), "알 수 없음")
                ))
                .collect(Collectors.toList());
    }
}
