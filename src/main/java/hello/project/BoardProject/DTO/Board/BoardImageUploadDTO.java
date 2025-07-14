package hello.project.BoardProject.DTO.Board;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class BoardImageUploadDTO {

    private List<MultipartFile> files; // 여러 개의 파일 업로드 정보를 담은 리스트
}