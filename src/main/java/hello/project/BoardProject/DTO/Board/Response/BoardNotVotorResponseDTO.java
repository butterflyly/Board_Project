package hello.project.BoardProject.DTO.Board.Response;

import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardNotVoter;
import hello.project.BoardProject.Entity.Users.Users;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BoardNotVotorResponseDTO {

    private Long id;
    private Board board;
    private Users notVoter;
    private LocalDateTime createDate;

    @Builder
    public BoardNotVotorResponseDTO(BoardNotVoter boardNotVoter) {
        this.id = boardNotVoter.getId();
        this.board = boardNotVoter.getBoard();
        this.notVoter = boardNotVoter.getNotVoter();
        this.createDate = boardNotVoter.getCreateDate();
    }
}
