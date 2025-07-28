package hello.project.BoardProject.Controller;

import hello.project.BoardProject.DTO.Users.UserRequestDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Users.UserRole;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import hello.project.BoardProject.Form.Users.UserRegisterForm;
import hello.project.BoardProject.OAuth2.Google.GoogleUserDetails;
import hello.project.BoardProject.OAuth2.Naver.NaverUserDetails;
import hello.project.BoardProject.OAuth2.OAuth2UserInfo;
import hello.project.BoardProject.OAuth2.PrincipalOAuth2UserService;
import hello.project.BoardProject.Repository.Users.ImageRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import hello.project.BoardProject.Service.Delete_UserService;
import hello.project.BoardProject.Service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OAuth2RegisterController {

    private final UserService userService;
    private final Delete_UserService deleteUserService;


    // OAuth2 로그인 시 최초 로그인인 경우 회원가입 진행, 필요한 정보를 쿼리 파라미터로 받는다
    @GetMapping("/oauth2/signUp")
    public String loadOAuthSignUp(@RequestParam String email, Model model,HttpServletRequest request) {

        UserResponseDTO users = userService.getUserEmailDTO(email);

        String nickname = "";

        if(users.getNickname().contains("(google)"))
        {
            nickname = users.getNickname().replace("(google)","");
        } else if (users.getNickname().contains("(naver)")) {
            nickname = users.getNickname().replace("(naver)","");
        }

        UserRegisterForm userRegisterForm = new UserRegisterForm();
        userRegisterForm.setNickname(nickname);
        userRegisterForm.setEmail(email);
        userRegisterForm.setUsername(users.getUsername());
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("email",email);

        model.addAttribute("userRegisterForm",userRegisterForm);
        model.addAttribute("email_Check",users.getEmail());

        return "users/signUp";
    }


    @PostMapping("/oauth2/signUp")
    public String loadOAuthSignUp(UserRegisterForm userRegisterForm,HttpSession httpSession) throws ServletException {

        userService.OAuth2Register(userRegisterForm);
        UserResponseDTO userResponseDTO = userService.getUserEmailDTO(userRegisterForm.getEmail());

        // 회원가입 완료 후 세션값 삭제
        httpSession.invalidate();

        // 회원가입 완료 이후 로그인
        if(userResponseDTO.getProviders().equals("google"))
        {
            return "redirect:/oauth2/authorization/google";
        }
        else {
            return "redirect:/oauth2/authorization/naver";
        }
    }

    /*
     이메일 인증
     */
    @PostMapping("/OAuth2/create/mail-auth")
    @ResponseBody
    public ResponseEntity<Void> MailAuth(UserRegisterForm userRegisterForm,
                                         HttpSession session)
    {
        String email_check = (String) session.getAttribute("email");

        // 소셜 로그인 이메일이랑 지정하려는 이메일이 같은 경우
        if(email_check.equals(userRegisterForm.getEmail()))
        {
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        // 작성하려는 이메일이 이미 다른회원이 사용하는 경우
        if(userService.checkEmail(userRegisterForm.getEmail())||
                deleteUserService.checkEmail(userRegisterForm.getEmail()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // 이메일이 중복되지 않은 경우
        else {
            String AuthNumber = String.valueOf(userService.sendMail(userRegisterForm.getEmail()));
            session.setAttribute("verificationCode", AuthNumber); // 세션에 전송된 인증번호를 저장.
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }
}
