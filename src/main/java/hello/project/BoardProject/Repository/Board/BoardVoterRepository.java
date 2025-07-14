package hello.project.BoardProject.Repository.Board;


import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardRead;
import hello.project.BoardProject.Entity.Board.BoardVoter;
import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardVoterRepository extends JpaRepository<BoardVoter,Long> {

    BoardVoter findByBoardAndVoter(Board board, Users users);


    List<BoardVoter> findByVoter(Users users);

    List<BoardVoter> findByBoard(Board board);

}


