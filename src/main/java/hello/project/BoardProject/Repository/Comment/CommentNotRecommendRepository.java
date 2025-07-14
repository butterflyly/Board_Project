package hello.project.BoardProject.Repository.Comment;


import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Comment.CommentNotRecommend;
import hello.project.BoardProject.Entity.Comment.CommentRecommend;
import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface CommentNotRecommendRepository extends JpaRepository<CommentNotRecommend,Long> {

    @Modifying
    @Query(value = "INSERT INTO comment_not_recommend(comment_id, user_id)" +
            " VALUES(:comment_id, :principal_id)", nativeQuery = true)
    int Notrecommend(Long comment_id, Long principal_id);

    @Modifying
    @Query(value = "DELETE FROM comment_not_recommend WHERE comment_id = " +
            ":comment_id AND user_id = :principal_id", nativeQuery = true)
    int cancelNotRecommend(Long comment_id, Long principal_id);

    CommentNotRecommend findByCommentAndUser(Comment comment, Users user);

    Set<CommentNotRecommend> findByComment(Comment comment);

    List<CommentNotRecommend> findByUser(Users users);
}


