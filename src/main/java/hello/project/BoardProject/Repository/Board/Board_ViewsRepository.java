package hello.project.BoardProject.Repository.Board;

import hello.project.BoardProject.Entity.Board.Board_Views;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface Board_ViewsRepository extends JpaRepository<Board_Views,Long> {
    List<Board_Views> findByCategoryAndViewsTimeBetweenOrderByViewsTimeAsc(int category,
                                                                           LocalDateTime oneMonthAgo, LocalDateTime now);
}
