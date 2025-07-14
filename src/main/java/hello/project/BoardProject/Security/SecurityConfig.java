package hello.project.BoardProject.Security;

import hello.project.BoardProject.OAuth2.PrincipalOAuth2UserService;
import hello.project.BoardProject.Service.ImageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;



@Configuration // 스프링의 환경 설정 파일임을 의미하는 애너테이션
@EnableWebSecurity // 모든 요청 URL이 스프링 시큐리티의 제어를 받도록 만드는 애너테이션
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final UserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll());

        http.formLogin((formLogin) -> formLogin
                .loginPage("/users/login")
                .successHandler(customLoginSuccessHandler));


        http.rememberMe(re -> re.key("remember-me")
                .userDetailsService(userDetailsService)
                        .tokenRepository(tokenRepository())
                .authenticationSuccessHandler(customLoginSuccessHandler));


        http.logout(logout -> logout
                .logoutUrl("/users/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession();
                    session.invalidate();
                })
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID","remember-me"));



        // OAuth 2.0 로그인 방식 설정
        http
                .oauth2Login((auth) -> auth.loginPage("/oauth-login/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/users/login")
                        .permitAll());

        return http.build();
    }




    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

}





