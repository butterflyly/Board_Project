package hello.project.BoardProject.DTO.Comment;


import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.Users;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRequestDTO {

    private Long id;
    private String content;
    private Boolean secret;
    private Board board;
    private Users users;
   // private Set<CommentRecommend> recommends;
   // private Set<CommentVoter> voter; // 추천
  //  private Set<CommentNotVoter> notvoter; // 비추천
    private Comment parent;
    private Boolean deleted;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;



    public Comment toEntity()
    {
        return Comment.builder().
                id(id).content(content)
                        .secret(secret).
                board(board).users(users)
             //           .recommends(recommends)
                //   notvoter(notvoter)
                        .parent(parent).deleted(deleted)
                        .createDate(createDate)
                        .modifyDate(modifyDate).
                build();
    }
}

