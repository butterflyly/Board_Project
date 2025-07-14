package hello.project.BoardProject.Form.Users;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageForm {

    @NotBlank(message = "수신자는 필수항목입니다.")
    private String receiverNickname;

    @NotBlank(message = "제목은 필수항목입니다.")
    private String title;

    @NotBlank(message = "내용은 필수항목입니다.")
    private String content;

}
