package hello.project.BoardProject.Entity.Board;

import hello.project.BoardProject.Entity.Users.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

// 읽음 처리 엔티티 (읽음 처리 테이블)
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "boardId")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "userId")
    private Users users;

}