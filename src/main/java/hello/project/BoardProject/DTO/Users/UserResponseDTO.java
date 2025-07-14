package hello.project.BoardProject.DTO.Users;

import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDTO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private LocalDateTime createDate;
    private String password;
    private UsersImage usersImage;
    private LocalDateTime user_delete_createDate;
    private String providers;

    public UserResponseDTO(Users users)
    {
        this.id = users.getId();
        this.username = users.getUsername();
        this.nickname = users.getNickname();
        this.email = users.getEmail();
        this.createDate = users.getCreateDate();
        this.password = users.getPassword();
        this.usersImage = users.getImage();
        this.user_delete_createDate = users.getUser_delete_createDate();
        this.providers = users.getProviders();
    }


}
