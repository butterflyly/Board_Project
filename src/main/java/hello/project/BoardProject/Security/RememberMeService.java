package hello.project.BoardProject.Security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

// Remember Me 서비스 (DB 저장)
public class RememberMeService implements PersistentTokenRepository {
    private final AuthenticationManager authenticationManager;

    public RememberMeService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @Override
    public void createNewToken(PersistentRememberMeToken token) {

    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {

    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        return null;
    }

    @Override
    public void removeUserTokens(String username) {

    }
}