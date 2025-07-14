package hello.project.BoardProject.OAuth2;


import hello.project.BoardProject.Entity.Users.Users;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {

    private final String name;
    private final String email;

    public SessionUser(Users user) {
        this.name = user.getUsername();
        this.email = user.getEmail();
    }
}