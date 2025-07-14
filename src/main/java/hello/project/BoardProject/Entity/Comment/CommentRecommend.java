package hello.project.BoardProject.Entity.Comment;


import hello.project.BoardProject.Entity.Users.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "comment_recommend")
public class CommentRecommend {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "comment_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

}


