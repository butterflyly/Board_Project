package hello.project.BoardProject.Controller.Users;

import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Form.Users.UserRegisterForm;
import hello.project.BoardProject.Service.Users.Delete_UserService;
import hello.project.BoardProject.Service.Users.OAuth2Service;
import hello.project.BoardProject.Service.Users.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.zip.DataFormatException;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final UserService userService;
    private final Delete_UserService deleteUserService;
    private final OAuth2Service oAuth2Service;


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
    public String loadOAuthSignUp(UserRegisterForm userRegisterForm,HttpSession httpSession)
            throws ServletException, DataFormatException {

        oAuth2Service.OAuth2Register(userRegisterForm);
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

    // 소셜로그인 회원탈퇴
    @PostMapping("/users/delete/OAuth2/{provider}")
    public String Delete_OAuth2(@PathVariable String provider, Principal principal,HttpSession httpSession) throws Exception {

        String accesToken = oAuth2Service.accessToken(principal.getName());


        // 구글 탈퇴
        if(provider.equals("google"))
        {
            if(accesToken != null)
            {
                oAuth2Service.revokeGoogleToken(accesToken);
            }
            else {
                throw new DataNotFoundException("액세스 토큰이 없어요. 재로그인으로 액세스 토큰을 재발급 받아주세요");
            }
        }
        // 네이버 탈퇴
        else {
            if(accesToken == null)
            {
                accesToken = oAuth2Service.reissueAccessToken(principal.getName());
            }
            oAuth2Service.NaverDelete(accesToken);
        }

        userService.UserDelete(principal.getName());

        httpSession.invalidate();

        return "redirect:/";
    }

}

