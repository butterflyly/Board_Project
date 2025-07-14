package hello.project.BoardProject.Repository.Board;


import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardNotVoter;
import hello.project.BoardProject.Entity.Board.BoardVoter;
import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardNotVoterRepository extends JpaRepository<BoardNotVoter,Long> {

    BoardNotVoter findByBoardAndNotVoter(Board board, Users users);


    List<BoardNotVoter> findByNotVoter(Users users);

    List<BoardNotVoter> findByBoard(Board board);


}


