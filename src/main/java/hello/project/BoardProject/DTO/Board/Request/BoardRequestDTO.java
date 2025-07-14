package hello.project.BoardProject.DTO.Board.Request;

import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Users.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardRequestDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private Users users;
    private int cateogory;
    private boolean fix;

    public Board toEntity()
    {
        return Board.builder().
                title(title).
                content(content).
                createDate(createDate).
                users(users).category(cateogory).fix(fix).
                build();
    }

    public Board ModifytoEntity()
    {
        return Board.builder().id(id).
                title(title).
                content(content).
                createDate(createDate).modifyDate(LocalDateTime.now()).
                users(users).
                build();
    }


}
