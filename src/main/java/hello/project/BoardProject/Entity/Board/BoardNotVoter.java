package hello.project.BoardProject.Entity.Board;


import hello.project.BoardProject.Entity.Users.Users;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class) //시간에 대해서 자동으로 값을 넣어주는 기능
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 기능: 클래스의 모든 필드를 매개변수로 받는 생성자를 생성합니다.
// 주로 의존성 주입이나 객체 생성 시 모든 필드를 초기화해야 할 때 사용
@AllArgsConstructor
/*
 게시글 비추천 데이터를 저장하는 엔티티
 */
public class BoardNotVoter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    private Users notVoter;

    @CreatedDate // 생성 시간 데이터 자동 주입
    private LocalDateTime createDate;


}


