package hello.project.BoardProject.DTO.Comment;


import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Comment.CommentImage;
import hello.project.BoardProject.Entity.Comment.CommentNotRecommend;
import hello.project.BoardProject.Entity.Comment.CommentRecommend;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String content;
    private String username;
    private String email;
    private String nickname;
    private Boolean secret;
    private Long user_Id;
    private Set<CommentRecommend> recommends;
    private Set<CommentNotRecommend> notRecommends;
    private List<Comment> children;
    private Comment parent;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private Boolean deleted;
    private Users users;
    private Board board;
    private String imageUrl;
    private int SecretNumber;
    private CommentImage commentImage;
    private String delete_user_nickname;

    @Builder
    public CommentResponseDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        if(comment.getUsers() != null)
        {
            this.username = comment.getUsers().getUsername();
            this.user_Id = comment.getUsers().getId();
            this.email = comment.getUsers().getEmail();
            this.nickname = comment.getUsers().getNickname();
            this.imageUrl = comment.getUsers().getImage().getUrl();
        }
        // 유저값이 null인 경우
        else
        {
            this.username = null;
            if(comment.getDelete_user_nickname() != null) {
                if (comment.getDelete_user_id() != null) {
                    this.nickname = comment.getDelete_user_nickname() + "(소프트 탈퇴 유저)";
                }
                // 삭제 유저 아이디가 null인 경우
                else {
                    this.nickname = comment.getDelete_user_nickname() + "(완전 탈퇴 유저)";
                }
            }
            // 유저값도 null이고 delete user nickname도 null인 경우
            else {
                this.nickname = "오류 댓글 닉네임";
            }
        }

        this.secret = comment.getSecret();
        this.recommends = comment.getRecommends();
        this.children = comment.getChildren();
        this.notRecommends = comment.getNot_recommends();
        this.parent = comment.getParent();
        this.createDate = comment.getCreateDate();
        this.modifyDate = comment.getModifyDate();
        this.deleted = comment.getDeleted();
        this.users = comment.getUsers();
        this.board = comment.getBoard();
        this.SecretNumber = comment.getSecretNumber();
        this.commentImage = comment.getImage();
        this.delete_user_nickname = comment.getDelete_user_nickname();

    }
}

