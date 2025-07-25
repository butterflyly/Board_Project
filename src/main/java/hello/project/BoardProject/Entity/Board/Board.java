package hello.project.BoardProject.Entity.Board;


import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.Users;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE board SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Slf4j
public class Board  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    @Column
    private Boolean deleted = Boolean.FALSE;

    @Setter
    private LocalDateTime deleteCreateDate;


    public void DeleteCreateDate(LocalDateTime localDateTime)
    {
        this.deleteCreateDate = localDateTime;
    }

    public void Deleted_False()
    {
        this.deleted = false;
        this.deleteCreateDate = null;
    }

    private Long delete_user_id;
    private String delete_user_nickname;
    private String hard_delete_user_nickname;

    public void UserDelete(Long id,String nickname)
    {
        delete_user_id = id;
        delete_user_nickname = nickname;
        users = null;
    }

    public void User_Hard_Delete(String nickname) {
        delete_user_id = null;
        hard_delete_user_nickname = nickname;
        delete_user_nickname = null;
        users = null;

    }

    public void UserReStore(Users user)
    {
        delete_user_id = null;
        delete_user_nickname = null;
        users = user;
    }

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    private List<BoardRead> boardReads;

    @ManyToOne
    private Users users;

    public void BoardUpdate(String title,String content,boolean fix)
    {
        this.title = title;
        this.content = content;
        this.fix = fix;
        modifyDate = LocalDateTime.now();
    }

    public void DeleteUserInfo(String nickname)
    {
        this.delete_user_nickname = nickname;
    }

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean fix;

    @Column
    private int category;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @OrderBy("id asc")
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int views;

    @Column(name = "voters")
    @OneToMany(mappedBy = "board",
            fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardVoter> voters = new HashSet<>();



    @Column(name = "not_voter")
    @OneToMany(mappedBy = "board",
            fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardNotVoter> notvoters = new HashSet<>();


    public void VoteMinus(BoardVoter boardVoter)
    {
        voters.remove(boardVoter);
    }

    public void NotVoterMinus(BoardNotVoter boardNotVoter)
    {
        notvoters.remove(boardNotVoter);
    }

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @OrderBy("id asc")
    private List<BoardImage> boardImages = new ArrayList<>();



    @Builder
    public Board(Long id, String title, String content, LocalDateTime createDate,
                 LocalDateTime modifyDate,Users users,int category,int views
    ,Set<BoardVoter> voters,Set<BoardNotVoter> notvoters,List<BoardImage> boardImages,
                 List<BoardRead> boardReads,boolean fix,String hard_delete_user_nickname,String delete_user_nickname)
    {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.users = users;
        this.category = category;
        this.views = views;
        this.voters = voters;
        this.notvoters = notvoters;
        this.boardImages = boardImages;
        this.boardReads = boardReads;
        this.fix = fix;
        this.hard_delete_user_nickname = hard_delete_user_nickname;
        this.delete_user_nickname = delete_user_nickname;
    }


}
