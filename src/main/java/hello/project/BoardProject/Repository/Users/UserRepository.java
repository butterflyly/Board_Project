package hello.project.BoardProject.Repository.Users;

import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByusername(String username);

    Boolean existsByNickname(String nickname);
    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);

    void deleteByUsername(String principal);

    List<Users> findByCreateDateBetweenOrderByCreateDateAsc(LocalDateTime oneMonthMinus, LocalDateTime now);

    Users findByNickname(String nickname);

    @Modifying
    @Transactional
    @Query(value = "delete from users u where u.user_id = :id",nativeQuery = true)
    void deleteById(@Param("id") Long id);
}
