package hello.project.BoardProject.DTO.Comment;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Comment_ImageUploadDTO {
    private MultipartFile file;

}
