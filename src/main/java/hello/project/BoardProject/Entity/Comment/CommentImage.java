package hello.project.BoardProject.Entity.Comment;


import hello.project.BoardProject.Entity.Users.Users;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comment_image")
/*
 댓글 이미지 엔티티
 */
public class CommentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String url;

    @OneToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    private Long hiddenCommentId;

}
