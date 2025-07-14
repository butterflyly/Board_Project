package hello.project.BoardProject.Repository.Board;



import hello.project.BoardProject.Entity.Board.BoardPageNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardPageRepository extends JpaRepository<BoardPageNumber, Long> {

    @Query(value = "SELECT * FROM(SELECT ID, " +
            "LAG(ID, 1, 0) OVER(ORDER BY ID ASC) AS PREVID, " +
            "LAG(title, 1, '이전글이 없습니다.') OVER (ORDER BY ID ASC) AS PREV_SUB," +
            "LEAD(ID, 1, 0) OVER(ORDER BY ID ASC) AS NEXTID, "+
            "LEAD(title, 1, '다음글이 없습니다') OVER (ORDER BY ID ASC) AS NEXT_SUB " +
            "FROM BOARD)a WHERE id = :id",
            nativeQuery = true) //오라클쿼리문그대로 쓰겠다
    BoardPageNumber findByPage(Long id);


}



/* LAG 함수 : 기준 데이터의 이전 행의 값을 반환해주는 함수
 * LEAD 함수 : 기준 데이터의 다음 행의 값을 반환해주는 함수 */
