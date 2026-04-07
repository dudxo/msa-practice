package com.msa.board.service;

import com.msa.board.client.UserServiceClient;
import com.msa.board.dto.CreateBoardRequest;
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

    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request) {
        Board board = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(request.getAuthorId())
                .build();

        Board savedBoard = boardRepository.save(board);
        String authorName = userServiceClient.getUserName(savedBoard.getAuthorId());
        return BoardResponse.from(savedBoard, authorName);
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
