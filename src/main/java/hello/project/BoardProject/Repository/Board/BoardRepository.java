package hello.project.BoardProject.Repository.Board;

import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board,Long> {

    @Modifying // @Modifying은 @Query로 INSERT, UPDATE, DELETE 및 DDL을 직접 작성하여 사용할 때 수정하여 직접 작성한 쿼리라는것을 명시
    @Query("update Board b set b.views = b.views + 1 where b.id = :id")
    @Transactional
    int updateView(Long id);

    List<Board> findByUsers(Users users);



    @Query("select "
            + "distinct b "
            + "from Board b "
            + "where "
            + "  ((b.category = :category) "
            + "   and ( "
            + "   b.title like %:kw% )"
            + "and b.fix = false)")
    Page<Board> findAllByTitleByKeywordAndType(
             @Param("kw") String kw, @Param("category") Integer category, Pageable pageable);


    @Query("select "
            + "distinct b "
            + "from Board b "
            + "where "
            + " (  (b.category = :category) "
            + "   and ( "
            + "   b.content like %:kw% "
            + " )" +
            "and b.fix = false)"
    )
    Page<Board> findAllByContentByKeywordAndType(@Param("kw") String kw, @Param("category") Integer category,
                                                 Pageable pageable);


    @Query("select "
            + "distinct b "
            + "from Board b "
            + "where "
            + "  ( (b.category = :category) "
            + "   and ( "
            + "   b.title like %:kw%  or  b.content like %:kw% ) and b.fix = false)" )
    Page<Board> findAllTitleOrContentByKeywordAndType(@Param("kw") String kw, @Param("category") Integer category,
                                                      Pageable pageable);

    @Query("select "
            + "distinct b "
            + "from Board b "
            + "left outer join Users u1 on b.users=u1 "
            + "where "
            + "((b.category = :category) "
            + "   and ( "
            + "    u1.nickname like %:kw% or b.delete_user_nickname like %:kw%) "
            + "and b.fix = false)"
        )
    Page<Board> findAllByNicknameByKeywordAndType(@Param("kw") String kw, @Param("category")
                                    int category, Pageable pageable);

    @Query("select "
            + "distinct b "
            + "from Board b "
            + "where "
            + "  ((b.category = :category) "
            + "   and ( "
            + "   b.title like %:kw% )"
            + "and b.fix = false) ORDER BY SIZE(b.voters) DESC")
    Page<Board> findAllByTitleByKeywordAndTypeAndVoters(
            @Param("kw") String kw, @Param("category") Integer category, Pageable pageable);

    @Query("select "
            + "distinct b "
            + "from Board b "
            + "where "
            + " (  (b.category = :category) "
            + "   and ( "
            + "   b.content like %:kw% "
            + " )" +
            "and b.fix = false) ORDER BY SIZE(b.voters) DESC"
    )
    Page<Board> findAllByContentByKeywordAndTypeAndVoters(@Param("kw") String kw, @Param("category") Integer category,
                                                 Pageable pageable);


    @Query("select "
            + "distinct b "
            + "from Board b "
            + "where "
            + "  ( (b.category = :category) "
            + "   and ( "
            + "   b.title like %:kw%  or  b.content like %:kw% ) and b.fix = false) ORDER BY SIZE(b.voters) DESC" )
    Page<Board> findAllTitleOrContentByKeywordAndTypeAndVoters(@Param("kw") String kw, @Param("category") Integer category,
                                                      Pageable pageable);

    @Query("select "
            + "distinct b "
            + "from Board b "
            + "left outer join Users u1 on b.users=u1 "
            + "where "
            + "((b.category = :category) "
            + "   and ( "
            + "    u1.nickname like %:kw% or b.delete_user_nickname like %:kw%)" +
            "and b.fix = false) ORDER BY SIZE(b.voters) DESC"
    )
    Page<Board> findAllByNicknameByKeywordAndTypeAndVoters(@Param("kw") String kw, @Param("category")
    int category, Pageable pageable);


    List<Board> findTop10ByOrderByCreateDateDesc();

    List<Board> findTop10ByCategoryOrderByCreateDateDesc(int category);

    Board findFirstByOrderByCreateDateAsc();

    // 생성 시간을 기준으로 다음 엔티티 조회
    @Query(value = "SELECT * FROM board b WHERE (b.create_date < :" +
            "currentTime) and (b.deleted = false) ORDER BY b.create_date DESC limit 1",nativeQuery = true)
    Optional<Board> findPreBoardByCreateDate(@Param("currentTime") LocalDateTime currentTime);

    @Query(value = "SELECT * FROM board b WHERE (b.create_date > " +
            ":currentTime) and (b.deleted = false) ORDER BY b.create_date ASC limit 1",nativeQuery = true)
    Optional<Board> findNextBoardByCreateDate(@Param("currentTime") LocalDateTime currentTime);



    List<Board> findByCategory(Integer category);

    Long countByUsers(Users users);

    List<Board> findTop5ByUsersOrderByCreateDateDesc(Users users);

    Page<Board> findByCategoryAndUsers(int cateogry,Users users,Pageable pageable);

    List<Board> findByCategoryAndFixOrderByCreateDateDesc(int category, boolean fix);

    Board findFirstByCategoryAndFixOrderByCreateDateAsc(int category,boolean fix);

    // 차트
    List<Board> findByCategoryAndCreateDateBetweenOrderByCreateDateAsc(int category,LocalDateTime oneMonthAgo, LocalDateTime now);

    List<Board> findTop3ByCategoryOrderByViewsDesc(int category, Sort sort);

    List<Board> findTop3ByOrderByViewsDesc(Sort sort);



    @Query(value = "SELECT b.* FROM board b " +
            "ORDER BY ((SELECT COUNT(*) FROM board_voter v1 WHERE v1.board_id = b.id) -" +
            " (SELECT COUNT(*) FROM board_not_voter v2 WHERE v2.board_id = b.id)) DESC , b.create_date DESC LIMIT 3", nativeQuery = true)
    List<Board> findByOrderByVotersCountMinusNotVotersCountDesc();


    @Query(value = "SELECT b.* FROM board b " +
            " where (b.category = :category) ORDER BY ((SELECT COUNT(*) FROM board_voter v1 WHERE v1.board_id = b.id) -" +
            " (SELECT COUNT(*) FROM board_not_voter v2 WHERE v2.board_id = b.id)) DESC , b.create_date DESC LIMIT 3", nativeQuery = true)
    List<Board> findByCategoryByOrderByVotersCountMinusNotVotersCountDesc(@Param("category") int category);

    @Query(value = "select count(*) from board b where (b.delete_user_id =:delete_user_id) and (b.deleted = false)",nativeQuery = true)
    Long countByDelete_User_Id(@Param("delete_user_id") Long delete_user_id);

    @Query(value = "select * from board b where (b.delete_user_id =:delete_user_id) and (b.deleted = false)" +
            " ORDER BY b.create_date DESC LIMIT 5"
    ,nativeQuery = true)
    List<Board> findTop5ByDelete_User_IdOrderByCreateDateDesc(@Param("delete_user_id") Long delete_user_id);

    @Query(value = "select * from board b where (b.delete_user_id =:delete_user_id) and (b.category =:category)" +
            "and (b.deleted = false)",nativeQuery = true)
    Page<Board> findByCategoryAndDelete_User_Id(@Param("category") int category, @Param("delete_user_id") Long delete_user_id,
                                                Pageable pageable);

    @Query(value = "select * from board b where (b.delete_user_id =:id) ",nativeQuery = true)
    List<Board> findByDelete_User_Id(@Param("id") Long id);


}
