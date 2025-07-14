package hello.project.BoardProject.Repository.Comment;



import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    Page<Comment> findAllByBoard(Pageable pageable, Board board);

    List<Comment> findTop5ByUsersOrderByCreateDateDesc(Users users);

    int countByUsers(Users users);

    int countByBoard(Board board);

    Page<Comment> findAllByUsers(Users users,Pageable pageable);

    List<Comment> findByUsers(Users users);

    List<Comment> findAllByBoardAndCreateDateBetweenOrderByCreateDateAsc(Board board,
                                                                         LocalDateTime oneMonthAgo, LocalDateTime now);


    @Query(value = "SELECT c.* FROM comment c" +
            " where ((SELECT COUNT(*) FROM comment_recommend r1 WHERE r1.comment_id = c.id) > 0 and (c.secret = false)" +
            " and (c.board_id = :board_id)) and (c.deleteboardid is null) " +
            "ORDER BY ((SELECT COUNT(*) FROM comment_recommend r1 WHERE r1.comment_id = c.id) -" +
            " (SELECT COUNT(*) FROM comment_not_recommend r2 WHERE r2.comment_id = c.id)) DESC , c.create_date DESC LIMIT 3", nativeQuery = true)
    List<Comment> findByOrderByRecommendCountMinusNotRecommendCountDesc(@Param("board_id") Long boardId);


    @Query(value = "select * from comment c where c.delete_user_id =:delete_user_id",nativeQuery = true)
    List<Comment> findByUserId(@Param("delete_user_id") Long id);

    @Query(value = "SELECT * FROM comment c" +
            " where (c.board_id = :board_id) ORDER BY ((SELECT COUNT(*) FROM comment_recommend r1 WHERE r1.comment_id = c.id) - (SELECT COUNT(*) FROM comment_not_recommend r2 WHERE r2.comment_id = c.id)) DESC , c.create_date DESC "
            , nativeQuery = true)
    Page<Comment> findAllByBoardOrderByRecommends(@Param("board_id") Long boardId,Pageable pageable);

    List<Comment> findTop5ByUsersAndBoardIsNotNullOrderByCreateDateDesc(Users users);

    int countByUsersAndBoardIsNotNull(Users users);

    Page<Comment> findAllByUsersAndBoardIsNotNull(Users users, Pageable pageable);

    List<Comment> findByBoard(Board board);

    @Query(value = "SELECT * FROM comment c where (c.delete_user_id =:delete_user_id) and (c.board_id is not null)" +
            " ORDER BY c.create_date DESC LIMIT 5",
            nativeQuery = true)
    List<Comment> findTop5ByDelete_User_IdAndBoardIsNotNullOrderByCreateDateDesc(@Param("delete_user_id") Long delete_user_id);

    @Query(value = "select count(*) from comment c where (c.delete_user_id =:delete_user_id)" +
            "and (c.board_id is not null)",nativeQuery = true)
    Long countByDelete_User_IdAndBoardIsNotNull(@Param("delete_user_id") Long delete_user_id);

    @Query(value = "select * from comment c where (c.delete_user_id =:id) and c.board_id is not null",nativeQuery = true)
    Page<Comment> findAllByDelete_User_Id(@Param("id") Long id, Pageable pageable);

    @Query(value = "select * from comment c where (c.delete_user_id =:id)",nativeQuery = true)
    List<Comment> findByDelete_User_Id(@Param("id") Long id);
}



