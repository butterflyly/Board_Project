package hello.project.BoardProject.Repository.Comment;

import hello.project.BoardProject.Entity.Comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HiddenCommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "select * from Comment c where c.deleteboardid =:boardId",nativeQuery = true)
    Page<Comment> findAllByBoard(Pageable pageable, @Param("boardId") Long boardId);

    @Query(value = "select * from Comment c where c.deleteboardid =:delete_board_id AND c.board_id is null",nativeQuery = true)
    List<Comment> findAllByBoard(@Param("delete_board_id") Long delete_board_id);

    @Modifying
    @Transactional
    @Query(value = "delete from comment b where b.id = :id",nativeQuery = true)
    void deleteById(@Param("id") Long id);
}
