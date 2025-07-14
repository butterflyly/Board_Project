package hello.project.BoardProject.Repository.Board;

import hello.project.BoardProject.Entity.Board.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeleteBoardRepository extends JpaRepository<Board,Long> {


    @Query(value = "select * from Board b where b.deleted = true",nativeQuery = true)
    Page<Board> findAll(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "delete from board b where b.id = :id",nativeQuery = true)
    void deleteById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "update board b set b.delete_user_id =:user_id ," +
            " b.users_user_id =null, b.delete_user_nickname =:nickname" +
            " where b.users_user_id =:user_id and b.deleted = true ",nativeQuery = true)
    void UserDelete(@Param("user_id") Long user_id, @Param("nickname") String nickname);

    @Modifying
    @Transactional
    @Query(value = "update board b set b.delete_user_id = null ," +
            " b.delete_user_nickname = null , b.hard_delete_user_nickname =:nickname" +
            " where b.id =:id and b.deleted = true",nativeQuery = true)
    void UserHardDelete(@Param("id") Long id,@Param("user_id") Long user_id,@Param("nickname") String nickname);


    @Query(value = "select * from Board b where b.deleted = true and (b.id =:id)",nativeQuery = true)
    Optional<Board> findById(@Param("id") Long id);


    @Query(value = "select * from board b where (b.deleted = true) and b.users_user_id =:user_id",nativeQuery = true)
    List<Board> findAllByUsers(@Param("user_id") Long user_id);

    @Query(value = "select * from board b where (b.deleted = true) and b.delete_user_id =:delete_user_id",nativeQuery = true)
    List<Board> findALlByDelete_User_Id(@Param("delete_user_id") Long delete_user_id);


    @Query(value = "select "
            + "* "
            + "from Board b "
            //           + "left outer join Users u1 on b.users=u1 "
            + "where b.deleted = true and "
            + "   (b.category = :category) "
            + "   and ( "
            + "   b.title like %:kw% )",nativeQuery = true)
    Page<Board> findAllByTitleByKeywordAndType(
            @Param("kw") String kw, @Param("category") Integer category, Pageable pageable);

    @Query(value = "select "
            + "* "
            + "from Board b "
            + "where b.deleted = true and"
            + "   (b.category = :category) "
            + "   and ( "
            + "   b.content like %:kw% "
            + " )",nativeQuery = true
    )
    Page<Board> findAllByContentByKeywordAndType(@Param("kw") String kw, @Param("category") Integer category,
                                                 Pageable pageable);


    @Query(value = "select "
            + "* "
            + "from Board b "
            + "where b.deleted = true and "
            + "   (b.category = :category) "
            + "   and ( "
            + "   b.title like %:kw%  or  b.content like %:kw% )",nativeQuery = true)
    Page<Board> findAllTitleOrContentByKeywordAndType(@Param("kw") String kw, @Param("category") Integer category,
                                                      Pageable pageable);

    @Query(value = "select * "
            + "from Board b "
            + " left outer join Users u1 on b.users_user_id = u1.user_id"
            + " where (b.deleted = true) and "
            + "   (b.category = :category) "
            + "   and (u1.nickname like %:kw% or b.delete_user_nickname like %:kw%) "
              ,nativeQuery = true
    )
    Page<Board> findAllByNicknameByKeywordAndType(@Param("kw") String kw, @Param("category")
    int category, Pageable pageable);

    @Query(value = "SELECT * FROM board b WHERE (b.create_date < :" +
            "currentTime) and (b.deleted = true) ORDER BY b.create_date DESC limit 1",nativeQuery = true)
    Optional<Board> findPreBoardByCreateDate(@Param("currentTime") LocalDateTime currentTime);

    @Query(value = "SELECT * FROM board b WHERE (b.create_date > " +
            ":currentTime) and (b.deleted = true) ORDER BY b.create_date ASC limit 1",nativeQuery = true)
    Optional<Board> findNextBoardByCreateDate(@Param("currentTime") LocalDateTime currentTime);

}
