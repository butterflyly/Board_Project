package hello.project.BoardProject.Repository.Users;

import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2AccesTokenDataRepository extends JpaRepository<OAuth2AccesTokenData,Long> {
    Optional<OAuth2AccesTokenData> findByUsername(String username);
}
