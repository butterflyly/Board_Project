package hello.project.BoardProject.Entity.Board;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "boardImage")
/*
 게시글 이미지 저장하는 엔티티
 */
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "BOARD_ID")
    private Board board;

    private Long delete_board_id;

    public void Board_Soft_Delete(Long id) {
        delete_board_id = id;
        board = null;
    }
}

