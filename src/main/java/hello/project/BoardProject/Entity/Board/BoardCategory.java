package hello.project.BoardProject.Entity.Board;


import lombok.Getter;

@Getter
public enum BoardCategory {

    QNA(0),
    FREE(1),
    TENDI(2);

    private int status;

    BoardCategory(int status)
    {
        this.status = status;
    }
}


