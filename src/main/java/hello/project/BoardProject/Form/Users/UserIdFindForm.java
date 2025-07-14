package hello.project.BoardProject.Form.Users;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIdFindForm {

    @NotBlank
    @Email(message = "이메일 형식을 지켜주세요")
    private String email;
}
