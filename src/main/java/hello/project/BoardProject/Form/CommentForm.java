package hello.project.BoardProject.Form;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/*
import com.minisite.PFProject.Users.Entity.Users;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;


 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentForm {


    private Long commentId;
    @NotBlank(message = "내용은 필수항목입니다.")
    private String content;

    private Boolean secret = false;
    private Long parentId;
    private Long commentUsers;
}
