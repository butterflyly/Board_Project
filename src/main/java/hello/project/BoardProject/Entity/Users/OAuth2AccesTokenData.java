package hello.project.BoardProject.Entity.Users;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
/*
   소셜 로그인 엑세스토큰 엔티티 데이터(추후 REDIS로 수정 후 코드 수정 및 삭제예정)
 */
public class OAuth2AccesTokenData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provier;

    private String username;

    private String token;

    @CreatedDate
    private LocalDateTime createDate;

}
