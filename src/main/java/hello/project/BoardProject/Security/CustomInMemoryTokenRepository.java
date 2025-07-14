package hello.project.BoardProject.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

public class CustomInMemoryTokenRepository extends InMemoryTokenRepositoryImpl {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        super.createNewToken(token);
        logger.info("New token created: {}", token.getUsername());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        super.updateToken(series, tokenValue, lastUsed);
        logger.info("Token updated - series: {}, lastUsed: {}", series, lastUsed);
    }
}