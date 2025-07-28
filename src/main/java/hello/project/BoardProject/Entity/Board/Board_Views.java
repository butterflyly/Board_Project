package hello.project.BoardProject.Entity.Board;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
/*
 게시글 조회가 얼마나 이루어졌는지 저장하는 엔티티
 */
public class Board_Views {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int category;
    private Long boardId;
    private LocalDateTime viewsTime;

}
