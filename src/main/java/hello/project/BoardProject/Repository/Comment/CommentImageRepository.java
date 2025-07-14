package hello.project.BoardProject.Repository.Comment;

import hello.project.BoardProject.Entity.Comment.CommentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommentImageRepository extends JpaRepository<CommentImage,Long> {

    @Transactional
    @Query(value = "UPDATE comment_image c SET c.comment_id = null Where c.id = :imageId"
    ,nativeQuery = true)
    void commentIdDelete(Long imageId);

    CommentImage findByComment_Id(Long commentId);
}
