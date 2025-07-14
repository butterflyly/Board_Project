package hello.project.BoardProject.Form.Users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateForm {

    @NotBlank
    @Size(min =2 ,max =10)
    private String nickname;
}
