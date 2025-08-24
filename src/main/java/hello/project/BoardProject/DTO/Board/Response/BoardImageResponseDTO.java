package hello.project.BoardProject.DTO.Board.Response;

import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardImage;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardImageResponseDTO {

    private Long id;

    private String url;

    private Board board;

    private Long delete_board_id;

    @Builder
    public BoardImageResponseDTO(BoardImage boardImage)
    {
        this.id = boardImage.getId();
        this.url = boardImage.getUrl();
        this.board = boardImage.getBoard();
        this.delete_board_id = boardImage.getDelete_board_id();
    }
}
