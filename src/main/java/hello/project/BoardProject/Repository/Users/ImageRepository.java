package hello.project.BoardProject.Repository.Users;


import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ImageRepository extends JpaRepository<UsersImage,Long> {

    UsersImage findByUsers(Users users);

    @Query(value = "select * from image i where i.delete_user_id =:id",nativeQuery = true)
    List<UsersImage> findByDelete_User_Id(@Param("id") Long id);

}


