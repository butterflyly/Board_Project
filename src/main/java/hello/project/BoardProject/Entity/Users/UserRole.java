package hello.project.BoardProject.Entity.Users;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    GUEST("ROLE_GUEST");

    UserRole(String value) {
        this.value = value;
    }

    private String value;
}