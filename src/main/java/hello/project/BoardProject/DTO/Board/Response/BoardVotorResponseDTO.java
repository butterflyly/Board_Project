package hello.project.BoardProject.DTO.Board.Response;

import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardVoter;
import hello.project.BoardProject.Entity.Users.Users;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BoardVotorResponseDTO {

    private Long id;
    private Board board;
    private Users voter;
    private LocalDateTime createDate;

    @Builder
    public BoardVotorResponseDTO(BoardVoter boardVoter) {
        this.id = boardVoter.getId();
        this.board = boardVoter.getBoard();
        if(boardVoter.getVoter() != null)
        {
            this.voter = boardVoter.getVoter();
        }
        this.createDate = boardVoter.getCreateDate();
    }
}
