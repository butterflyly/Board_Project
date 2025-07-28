package hello.project.BoardProject.Entity.Comment;


import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comment")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true) // 기존 Builder로 생성된 객체의 값 일부를 변경하여 새로운 객체를 생성
/*
 댓글 엔티티
 */
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "BOARD_ID")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private Users users;

    @Builder.Default
    private Boolean secret = false;

    @Setter
    @OneToOne(mappedBy = "comment", fetch = FetchType.LAZY,orphanRemoval = true)
    private CommentImage image;

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE,fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<CommentRecommend> recommends = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE,fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<CommentNotRecommend> not_recommends = new HashSet<>();


    @Column(columnDefinition = "integer default 0", nullable = false)
    private int SecretNumber;

    public void SecretNumber(int i)
    {
        this.SecretNumber = i;
    }


    @Column(name = "Deleteboardid")
    private Long deleteboardid;

    private Long delete_user_id;

    private String delete_user_nickname;

    // 게시글을 소프트 삭제한 경우
    public void Board_Soft_Delete(Long id)
    {
        deleteboardid = id;
        board = null;
    }

    // 유저를 소프트 삭제할 경우
    public void UserDelete(Long id,String nickname)
    {
        delete_user_id =id;
        delete_user_nickname = nickname;
        users = null;
    }

    // 유저 하드삭제할 경우
    public void UserDelete() {
        delete_user_id =null;
    }

    // 유저를 복원하는 매소드
    public void UserReStore(Users users)
    {
        this.users = users;
        delete_user_id = null;
        delete_user_nickname = null;
    }

    // 소프트 삭제 유저가 수정되는 경우
    public void DeleteUserInfo(String nickname)
    {
        this.delete_user_nickname = nickname;
    }


    @ToString.Exclude
    @ManyToOne
    private Comment parent;

    // 삭제 여부 나타내는 속성 추가
    @Builder.Default
    private Boolean deleted = false;

    // Cascade REMOVE 불가 : 자식 댓글이 있는 상태에서, 그냥 댓글 삭제하면 자식 댓글 전부 지워짐
    // OrphanRemoval로 대댓글과 연관관계 끊어지면 삭제되게 설정
    @OneToMany(mappedBy = "parent",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default // 빌더패턴 리스트시 초기화
    private List<Comment> children = new ArrayList<>();

    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    // 댓글 수정
    public void update(String content,boolean secret) {
        this.content = content;
        this.secret = secret;
        this.modifyDate = LocalDateTime.now();
    }

    public void deleteParent() {
        deleted = true;
    }


    // 타임리프에서 비밀 댓글이면 댓글의 내용이 안보이게 하기 위함
    public boolean isSecret() {
        return this.secret == true;
    }

    // 타임리프에서 삭제 댓글이면 댓글의 내용이 안보이게 하기 위함
    public boolean isDeleted() {
        return this.deleted == true;
    }

}

