package hello.project.BoardProject.Entity.Board;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BoardPageNumber {

    @Id
    private Long id;
    private String PREVID;
    private String PREV_SUB;
    private String NEXTID;
    private String NEXT_SUB;

}


