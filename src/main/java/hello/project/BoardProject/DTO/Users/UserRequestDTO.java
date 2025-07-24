package hello.project.BoardProject.DTO.Users;

import hello.project.BoardProject.Entity.Users.UserRole;
import hello.project.BoardProject.Entity.Users.Users;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserRequestDTO {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private LocalDateTime createDate;
    private UserRole userRole;
    private String providers;
    private String providerIds;

    public Users toEntity()
    {
        return Users.builder().
                username(username).password(password)
                        .nickname(nickname).email(email)
                        .createDate(LocalDateTime.now()).userRole(userRole).
                build();
    }

    public Users ModifytoEntity()
    {
        return Users.builder().id(id).
                username(username)
                .nickname(nickname).email(email).userRole(userRole).
                providers(providers).providerIds(providerIds).createDate(createDate).
                build();
    }
}
