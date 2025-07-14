package hello.project.BoardProject.DTO.Board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BoardSeachDTO {

    private String keyword; // 검색 키워드
    private String type; // 검색 타입
}
