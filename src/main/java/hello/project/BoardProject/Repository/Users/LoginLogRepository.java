package hello.project.BoardProject.Repository.Users;

import hello.project.BoardProject.Entity.Users.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Integer> {

    List<LoginLog> findByLoginlogTimeBetweenOrderByLoginlogTime(LocalDateTime oneMonthAgo, LocalDateTime now);
}