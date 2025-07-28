package hello.project.BoardProject.Security;


import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Repository.Users.OAuth2AccesTokenDataRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import hello.project.BoardProject.Service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final UserRepository userRepository;
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        String username = authentication.getName();

        Users users = userRepository.findByusername(username).orElseThrow(() -> new IllegalArgumentException("로그아웃하려는 유저가 앖음"));

        Optional<OAuth2AccesTokenData> auth2AccesTokenData = oAuth2AccesTokenDataRepository.findByUsername(users.getUsername());

        // 엑세스 토큰 삭제
        if(!auth2AccesTokenData.isEmpty())
        {
            OAuth2AccesTokenData oAuth2AccesTokenData = auth2AccesTokenData.get();
            oAuth2AccesTokenDataRepository.delete(oAuth2AccesTokenData);
        }

        // 이전 페이지 URL 가져오기 (Referer 헤더 활용)
        String referer = request.getHeader("Referer");
        log.info("referer :" + referer);
        // 이전 페이지가 없거나, 로컬 호스트가 아니면 기본 URL로 이동

        if (referer == null || !referer.contains("localhost") || !referer.startsWith("http://")) {
            log.info("로그아웃 에러");
            response.sendRedirect("/");
        } else {
            log.info("로그아웃 성공");
            response.sendRedirect(referer);
        }
    }
}

