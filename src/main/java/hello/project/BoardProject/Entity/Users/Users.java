package hello.project.BoardProject.Entity.Users;

import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardNotVoter;
import hello.project.BoardProject.Entity.Board.BoardRead;
import hello.project.BoardProject.Entity.Board.BoardVoter;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Comment.CommentNotRecommend;
import hello.project.BoardProject.Entity.Comment.CommentRecommend;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET user_deleted = true WHERE user_id = ?")
@Where(clause = "user_deleted = false")
/*
  유저 엔티티
 */
public class Users {


    @Column(name = "userId")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column
    private String password;

    @Column(unique = true)
    private String nickname;

    @Column(unique = true)
    private String email;

    @Column(name="user_createDate")
    private LocalDateTime createDate;

    @OneToOne(mappedBy = "users", fetch = FetchType.LAZY)
    private UsersImage image;

    @Column(name = "provider")
    // provider : google, kakao, naver 이 들어감
    private String providers;

    @Column(name = "providerId")
    // providerId : 구굴 로그인 한 유저의 고유 ID가 들어감
    private String providerIds;

    private UserRole userRole;

    @OneToMany(mappedBy = "users", fetch = FetchType.LAZY)
    @OrderBy("id")
    private List<Board> boardList = new ArrayList<>();

    // 유저 삭제시 추천,비추천 삭제
    @OneToMany(
            fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardVoter> boardVoterList = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardNotVoter> boardNotVoterList = new ArrayList<>();


    @Column(name="user_modifyDate")
    private LocalDateTime modifyDate;

    @Column(name = "user_deleted")
    private Boolean deleted = Boolean.FALSE;


    public void NicknameUpdate(String nickname)
    {
        this.nickname = nickname;
        modifyDate = LocalDateTime.now();
    }

    public void PasswordUpdate(String password)
    {
        this.password = password;
        modifyDate = LocalDateTime.now();
    }

    private LocalDateTime user_delete_createDate;

    @OneToMany(mappedBy = "sender",fetch = FetchType.LAZY)
    private List<Message> sentMessages;

    @OneToMany(mappedBy = "receiver",fetch = FetchType.LAZY)
    private List<Message> receivedMessages;

    @OneToMany(mappedBy = "users" ,fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardRead> boardReadList = new ArrayList<>();

    @OneToMany
    private List<Comment> commentList = new ArrayList<>();

    // 유저 삭제시 댓글 추천,비추천 삭제
    @OneToMany(
            fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentRecommend> commentRecommends = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentNotRecommend> commentNotRecommends = new ArrayList<>();



    @Builder
    public Users(Long id, String username, String password, String
            nickname, String email, LocalDateTime createDate,
                 String providers, String providerIds, UserRole userRole, UsersImage image
                ,LocalDateTime user_delete_createDate,List<Message> sentMessages,
                 List<Message> receivedMessages
    ,List<BoardRead> boardReads,List<BoardVoter> boardVoterList,List<BoardNotVoter> boardNotVoterList,
                 List<CommentRecommend> commentRecommends,List<CommentNotRecommend> commentNotRecommends)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.createDate = createDate;
        this.providers = providers;
        this.providerIds = providerIds;
        this.userRole = userRole;
        this.image = image;
        this.user_delete_createDate = user_delete_createDate;
        this.sentMessages = sentMessages;
        this.receivedMessages = receivedMessages;
        this.boardReadList = boardReads;
        this.boardVoterList = boardVoterList;
        this.boardNotVoterList = boardNotVoterList;
        this.commentRecommends = commentRecommends;
        this.commentNotRecommends = commentNotRecommends;
    }

    public void Deleted_False() {
        deleted = false;
        this.user_delete_createDate = null;
    }

    public void Delete_Time(LocalDateTime now) {
        this.user_delete_createDate = now;
    }
}