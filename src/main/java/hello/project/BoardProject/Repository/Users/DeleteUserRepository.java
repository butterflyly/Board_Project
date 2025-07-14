package hello.project.BoardProject.Repository.Users;

import hello.project.BoardProject.Entity.Users.Users;
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

public interface DeleteUserRepository extends JpaRepository<Users, Long> {


    @Transactional
    @Modifying
    @Query(value = "Update Users u set u.user_delete_create_date =:user_delete_create_date where user_id =:id",
            nativeQuery = true)
    void save(@Param("id") Long id, @Param("user_delete_create_date") LocalDateTime user_delete_create_date);

    @Query(value = "select * from Users u where u.user_deleted = true",nativeQuery = true)
    Page<Users> findAll(Pageable pageable);

    @Query(value = "select * from Users u where (u.user_deleted = true) and u.username = :username",nativeQuery = true)
    Optional<Users> findByusername(@Param("username") String username);

    @Query(value = "select * from Users u where u.username = :username",nativeQuery = true)
    Optional<Users> findByusernameOAuth2(@Param("username") String username);

    @Query(value = "select * from Users u where (u.user_deleted = true) and u.user_id = :user_id",nativeQuery = true)
    Optional<Users> findById(@Param("user_id") Long user_id);

    @Query(value = "select count(*) FROM Users u where (u.user_deleted = true) and u.nickname = :nickname",nativeQuery = true)
    int existsByNickname(@Param("nickname") String nickname);

    @Query(value = "select count(*) FROM Users u where (u.user_deleted = true) and u.username = :username",nativeQuery = true)
    int existsByUsername(@Param("username") String username);

    @Query(value = "select count(*) FROM Users u where (u.user_deleted = true) and u.email = :email",nativeQuery = true)
    int existsByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "delete from users u where u.user_id = :id",nativeQuery = true)
    void deleteById(@Param("id") Long id);

    @Query(value = "select * from users u where u.user_deleted = true",nativeQuery = true)
    List<Users> findAll();


    @Query(value = "SELECT * FROM users u WHERE (u.user_delete_create_Date " +
            "BETWEEN :startDate AND :endDate) and (u.user_deleted = true) ",nativeQuery = true)
    List<Users> findByDeleteUsers(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);


    @Modifying
    @Transactional
    @Query(value = "Update Users u set u.nickname =:nickname where user_id =:id",
            nativeQuery = true)
    void save(@Param("id") Long id, @Param("nickname") String nickname);
}

