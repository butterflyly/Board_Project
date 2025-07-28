package hello.project.BoardProject.Entity.Users;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "image")
/*
 유저 이미지 엔티티
 */
public class UsersImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    @JoinColumn(name = "User_ID")
    private Users users;

    public void updateUrl(String url) {
        this.url = url;
    }

    private Long delete_user_id;

    public void UserDelete(Long id)
    {
        delete_user_id = id;
        users = null;
    }

}

