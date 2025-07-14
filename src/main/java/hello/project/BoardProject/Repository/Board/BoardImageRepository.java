package hello.project.BoardProject.Repository.Board;


import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {

    @Query(value = "select * from board_image b where b.delete_board_id =:delete_board_id"
    ,nativeQuery = true)
    List<BoardImage> findByDelete_Board_Id(@Param("delete_board_id") Long delete_Board_Id);

    List<BoardImage> findByBoard(Board board);
}

