package hello.project.BoardProject.Security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class PersistentLogins {

    @Id
    private String series;
    private String username;
    private String token;
    private LocalDateTime lastUsed;
}