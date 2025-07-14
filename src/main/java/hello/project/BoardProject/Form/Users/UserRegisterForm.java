package hello.project.BoardProject.Form.Users;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserRegisterForm {

    @NotBlank(message = "아이디는 필수 입력값입니다")
  //  @Pattern(regexp = "^[a-z0-9A-Z]{5,15}$", message = "아이디는 영어와 숫자만 사용하여 5~15자리여야 합니다.")
    private String username;

    @NotBlank(message = "닉네임은 필수 입력값입니다")
  //  @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수 입력값입니다")
  //  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]" +
  //          "{8,16}$", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;
    private String password2;

    @NotBlank
  //  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    private String inputCode;

}
