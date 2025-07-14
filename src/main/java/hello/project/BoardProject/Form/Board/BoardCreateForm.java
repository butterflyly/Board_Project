package hello.project.BoardProject.Form.Board;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class BoardCreateForm {

    private Long id;

    @NotBlank // 띄어쓰기까지 포함하지 않음
    @Size(min = 2, max =20, message = "제목은 2자이상 20자이하로 작성해주세요")
    private String title;

    @NotBlank // 띄어쓰기까지 포함하지 않음
    @Size(min = 2, max =300,  message = "내용은 2자이상 300자이하로 작성해주세요")
    private String content;

    private boolean fix = false;
    private String nickname;

    private List<MultipartFile> files; // 여러 개의 파일 업로드 정보를 담은 리스트


}
