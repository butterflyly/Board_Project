package hello.project.BoardProject.Form.Users;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserModifyForm {

    private Long id;

    @NotBlank
    @Size(min=2, max=10, message = "닉네임은 2~10자 사이로 입력해주세요")
    private String nickname;
}
