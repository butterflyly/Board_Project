package hello.project.BoardProject.DTO.Board.Response;

import hello.project.BoardProject.Entity.Board.*;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.Users;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
/*
  게시글 RESPONSE DTO
 */
public class BoardResponseDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private Users users;
    private int category;
    private int views;
    private String username;
    private String nickname;

    private Set<BoardVoter> voters;
    private Set<BoardNotVoter> notvoters;


    // 이부분 추가
    private List<String> imageUrls;
    private List<BoardImage> boardImages;
    private LocalDateTime delete_createDate;
    private List<BoardRead> boardReadList;
    private boolean fix;
    private List<Comment> comments;
    private String delete_user_nickname;

    @Builder
    public BoardResponseDTO(Board board)
    {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.createDate = board.getCreateDate();
        this.modifyDate = board.getModifyDate();
        if(board.getUsers() != null)
        {
            this.users = board.getUsers();
        }
        else {
            this.users = null;
        }

        if(board.getUsers() != null)
        {
            this.username = board.getUsers().getUsername();
        }
        else {
            this.username = "탈퇴유저";
        }

        if(board.getUsers() != null)
        {
            this.nickname = board.getUsers().getNickname();
        }
        else {
            if(board.getDelete_user_nickname() == null)
            {
                this.nickname = board.getHard_delete_user_nickname();
            }
            else {
                this.nickname = board.getDelete_user_nickname();
            }
        }

        this.category = board.getCategory();
        this.views = board.getViews();
        this.voters = board.getVoters();
        this.notvoters = board.getNotvoters();
        // 이부분 추가
        this.imageUrls = board.getBoardImages().stream()
                .map(BoardImage::getUrl).collect(Collectors.toList());
        this.boardImages = board.getBoardImages();
        this.delete_createDate = board.getDeleteCreateDate();
        this.fix = board.isFix();
        this.boardReadList = board.getBoardReads();
        this.comments = board.getComments();
        this.delete_user_nickname = board.getDelete_user_nickname();
    }
}
