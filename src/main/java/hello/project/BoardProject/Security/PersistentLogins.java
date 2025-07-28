package hello.project.BoardProject.Security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
/*
 어디에 쓰는 엔티티인지 기억이 안남 일단 냅둠
 */
public class PersistentLogins {

    @Id
    private String series;
    private String username;
    private String token;
    private LocalDateTime lastUsed;
}