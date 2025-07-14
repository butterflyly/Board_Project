package hello.project.BoardProject.Repository.Board;

import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardRead;
import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardReadRepository  extends JpaRepository<BoardRead, Long> {


    List<BoardRead> findByUsers(Users users);

    Optional<BoardRead> findByUsersAndBoard(Users users,Board board);

    @Query(value = "select * from board_read b where b.user_id =:user_id",nativeQuery = true)
    List<BoardRead> findByUsersId(@Param("user_id") Long user_id);

    @Query(value = "select * from board_read b where (b.delete_user_id =:id)",nativeQuery = true)
    List<BoardRead> findByDelete_User_Id(Long id);
}