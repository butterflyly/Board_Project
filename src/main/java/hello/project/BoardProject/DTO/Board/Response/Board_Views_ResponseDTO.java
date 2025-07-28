package hello.project.BoardProject.DTO.Board.Response;


import hello.project.BoardProject.Entity.Board.Board_Views;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
/*
    게시글 VIEWS 엔티티 Response DTO 클래스
 */
public class Board_Views_ResponseDTO {

    private Long id;
    private int category;
    private Long boardId;
    private LocalDateTime viewsTime;

    @Builder
    public Board_Views_ResponseDTO(Board_Views boardViews)
    {
        this.id = boardViews.getId();
        this.category = boardViews.getCategory();
        this.boardId = boardViews.getBoardId();
        this.viewsTime = boardViews.getViewsTime();
    }
}
