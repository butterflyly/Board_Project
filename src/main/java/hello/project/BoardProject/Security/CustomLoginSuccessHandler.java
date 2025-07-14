package hello.project.BoardProject.Security;

import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Service.LoginLogService;
import hello.project.BoardProject.Service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
// AuthenticationSuccessHandler :  스프링 시큐리티에서 인증이 성공했을 때 구현하여 적용할 수 있는 인터페이스
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private RequestCache requestCache = new HttpSessionRequestCache(); // Spring Security에서 인증 실패 시 이전 요청 정보를 저장하고 사용하기 위해 사용하는 코드
    private final LoginLogService loginLogService;

    // Spring Security에서 화면 이동 규칙을 정의하는 인터페이스인 RedirectStrategy를 사용하는 코드
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, FilterChain chain,
                                        Authentication authentication) throws IOException,
            ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain,
                authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException,
            ServletException {
        log.info("onAuthenticationSuccess");
        SavedRequest savedRequest = requestCache.getRequest(request, response); // Spring Security에서 사용자의 요청 정보를 저장하는 인터페이스
        Object principal = authentication.getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername(); // 유저 이름

        // 접근 권한 없는 경로 접근해서 스프링 시큐리티가 인터셉트해서 로그인폼으로 이동 후 로그인 성공한 경우
        if (savedRequest != null) { // 클라이언트가 요청한 링크
            String targetUrl = savedRequest.getRedirectUrl();
            log.info("targetUrl = {}", targetUrl);
            redirectStrategy.sendRedirect(request, response, targetUrl); // 해당 링크를 RedirectStrategy 에 저장
        }
        // 로그인 버튼 눌러서 로그인한 경우 기존에 있던 페이지로 리다이렉트
        else {
            String prevPage = (String) request.getSession().getAttribute("prevPage");
            log.info("prevPage = {}", prevPage);
            redirectStrategy.sendRedirect(request, response, prevPage);
        }

        loginLogService.loginLogSave(username);

    }

    // 클라이언트 IP 주소 가져오는 메소드
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
