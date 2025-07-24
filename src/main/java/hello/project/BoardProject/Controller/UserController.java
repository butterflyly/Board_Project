package hello.project.BoardProject.Controller;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.ImageUploadDTO;
import hello.project.BoardProject.DTO.Users.ImageResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Form.Users.PWChangeForm;
import hello.project.BoardProject.Form.Users.PWCheckForm;
import hello.project.BoardProject.Form.Users.UserModifyForm;
import hello.project.BoardProject.Form.Users.UserRegisterForm;
import hello.project.BoardProject.Repository.Users.OAuth2AccesTokenDataRepository;
import hello.project.BoardProject.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import static javax.security.auth.callback.ConfirmationCallback.OK;


@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final BoardService boardService;
    private final CommentService commentService;
    private final ImageService imageService;
    private final Delete_UserService deleteUserService;
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;

    /*
    유저 회원가입 GetMapping(C)
     */
    @GetMapping("/create")
    public String UsersCreate(Model model,Principal principal)
    {
        // 로그인한 유저인 경우 메인페이지로
        if(principal != null)
        {
            return "redirect:/";
        }

        model.addAttribute("userRegisterForm",new UserRegisterForm());
        return "users/create";
    }

    /*
    유저 회원가입 PostMapping(C)
     */
    @PostMapping("/create")
    public String UsersCreate(UserRegisterForm userRegisterForm,Principal principal,HttpSession httpSession)
    {
        if(principal != null)
        {
            return "redirect:/";
        }

        userService.UserCreate(userRegisterForm.getUsername(),userRegisterForm.getPassword(),
               userRegisterForm.getEmail(),userRegisterForm.getNickname(),LocalDateTime.now());

        httpSession.removeAttribute("verificationCode"); // 인증번호 세션 제거
        return "redirect:/board/list/qna";
    }


    /*
    아이디 중복체크
     */
    @ResponseBody
    @PostMapping("/create/check-duplicate-id")
    public boolean checkDuplicateId(@RequestParam String username) {

        // 삭제 테이블에 유저이름이 있거나 , 기존 테이블에 유저 이름이 있으면 false 반환
        if(deleteUserService.checkUsername(username) || userService.checkUsername(username))
        {
            return false;
        }
        else {
            return true;
        }
    }

    /*
     닉네임 중복 체크
    */
    @ResponseBody
    @PostMapping("/create/check-duplicate-nickname")
    public boolean checkDuplicateNickname(@RequestParam String nickname) {

        if(deleteUserService.checkNickname(nickname) || userService.checkNickname(nickname))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /*
    이메일 중복체크
    */
    @PostMapping("/create/mail-auth")
    @ResponseBody
    public ResponseEntity<Void> MailAuth(UserRegisterForm userRegisterForm, HttpSession session)
    {
        if(userService.checkEmail(userRegisterForm.getEmail())|| deleteUserService.checkEmail(userRegisterForm.getEmail()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String AuthNumber = String.valueOf(userService.sendMail(userRegisterForm.getEmail()));
        session.setAttribute("verificationCode", AuthNumber); // 세션에 전송된 인증번호를 저장.
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /*
     인증번호 일치 확인
     */
    @PostMapping("/mailCheck")
    public ResponseEntity<Boolean> verifyCode(@RequestParam("inputCode") String inputCode, HttpSession session) {
        String storedCode = (String) session.getAttribute("verificationCode");

        if (storedCode != null && storedCode.equals(inputCode)) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    // 로그인
    @GetMapping("/login")
    public String login(HttpServletRequest request,Principal principal) {

        if(principal != null)
        {
            return "redirect:/";
        }

        String prevPage = request.getHeader("Referer"); // 현재 페이지 오기 전 URL 정보

        log.info("loginForm prevPage = {}", prevPage); // 패아지 링크 URL 로그 출력
        if(prevPage != null && !prevPage.contains("/users/login")) { // 이전 페이지가 로그인 페이지가 아니고 이전 페이지 정보가 null이 아닌 경우
            request.getSession().setAttribute("prevPage", prevPage); // 이전 페이지 정보 세션에 저장
        }

        // request의 어트리뷰트에 저장하면 클라이언트에 응답이 된후에는 해당 어트리뷰트는 사라지기 때문에 세션에 저장해야 함

        return "users/login";
    }

    /*
     회원 조회
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage")
    public String MyPage(Principal principal, Model model)
    {
        UserResponseDTO users = userService.getUserDTO(principal.getName());

        model.addAttribute("user", users);

        ImageResponseDTO image = imageService.findImage(users.getEmail()); // 유저 이미지
        model.addAttribute("image", image);

         Long boardCount = boardService.getBoardCount(users); // 게시글 개수 삭제한 게시글 개수는 포함안되게
         model.addAttribute("boardCount", boardCount);

        List<BoardResponseDTO> boardList = boardService.getBoardTop5LatestByUser(users); // 최신 5개 게시글
        model.addAttribute("boardList", boardList);

        List<CommentResponseDTO> commentList = commentService.getCommentTop5LatestByUser(users); // 최신 5개 댓글
        model.addAttribute("commentList",commentList);

        int commentCount = commentService.getCommentCount(users);
        model.addAttribute("commentCount",commentCount);

        return "users/my_page";
    }

    /*
      유저 수정
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public String user_update(Principal principal,Model model)
    {
        UserModifyForm userModifyForm = new UserModifyForm();
        userModifyForm.setNickname(userService.getUserDTO(principal.getName()).getNickname());

        model.addAttribute("userModifyForm", userModifyForm);
        ImageResponseDTO image = imageService.findImage(userService.getUserDTO(principal.getName()).getEmail());
        model.addAttribute("image", image);

        return "users/user_update";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/info")
    public String user_update(@Valid UserModifyForm userModifyForm,
                             BindingResult bindingResult,
                             @ModelAttribute ImageUploadDTO imageUploadDTO,
                             Principal principal,Model model) {

        UserResponseDTO users = userService.getUserDTO(principal.getName());
        ImageResponseDTO image = imageService.findImage(users.getEmail());


        if (bindingResult.hasErrors()) {
            model.addAttribute("image", image);
            return "users/user_update";
        }

        log.info("imageUploadDTO is {}", imageUploadDTO);
        // 닉네임은 수정을 안함(수정폼 닉네임 == 기존 닉네임)
        if(userModifyForm.getNickname().equals(users.getNickname()))
        {
            imageService.upload(imageUploadDTO,users.getEmail());

            return "redirect:/users/mypage";
        }
        // 닉네임 수정함
        else {
            // 여기서 중복된 닉네임 체크
            if(userService.checkNickname(userModifyForm.getNickname()) ||
                    deleteUserService.checkNickname(userModifyForm.getNickname()))
            {
                model.addAttribute("image", image);
                bindingResult.reject("modify_Failed","중복된 닉네임입니다");
                return "users/user_update";
            }
            imageService.upload(imageUploadDTO,users.getEmail());
            userService.NicknameUpdate(userModifyForm.getNickname(),users.getUsername());
            return "redirect:/users/mypage";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/pwchange")
    public String PWChange(@ModelAttribute("pwChangeForm") PWChangeForm pwChangeForm)
    {
        return "users/pw_change";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/pwchange")
    public String PWChange(@Valid @ModelAttribute("pwChangeForm")PWChangeForm pwChangeForm
            , BindingResult bindingResult, Principal principal) {

        if (bindingResult.hasErrors()) {
            return "users/pw_change";
        }

        UserResponseDTO user = userService.getUserDTO(principal.getName());

        // 이전 패스워드와 맞지 않을경우
        if (!userService.checkPassword(user, pwChangeForm.getPrePassword())) {
            bindingResult.reject("notMatchPW", "이전 비밀번호가 일치하지 않습니다.");
            return "users/pw_change";
        }
        // 새 비밀번호, 비밀번호 확인 창 일치하지 않을경우
        if (!pwChangeForm.getNewPassword1().equals(pwChangeForm.getNewPassword2())) {
            bindingResult.reject("notMatchNewPW", "새 비밀번호와 확인이 일치하지 않습니다.");
            return "users/pw_change";
        }

        userService.PWChange(user, pwChangeForm.getNewPassword1());

        return "redirect:/users/mypage";
    }

    @GetMapping("/delete/checkPwdForm")
    @PreAuthorize("isAuthenticated()")
    public String DeletecheckPwdView(Principal principal,HttpSession session,Model model) {
        UserResponseDTO user = userService.getUserDTO(principal.getName());
        session.getAttribute("usersId");
        model.addAttribute("error",false);

        if (user == null) {
            throw new DataNotFoundException("사용자를 찾을 수 없습니다.");
        }
        return "users/delete_check_pwd";
    }

    @PostMapping("/delete/checkPwdForm")
    @PreAuthorize("isAuthenticated()")
    public String DeletecheckPwd(@Valid PWCheckForm pwCheckForm,
                                 Principal principal,
                                 HttpSession session,Model model)
    {
        UserResponseDTO user = userService.getUserDTO(principal.getName());

        if (user == null) {
            throw new DataNotFoundException("사용자를 찾을 수 없습니다.");
        }

        model.addAttribute("password",pwCheckForm.getPrePassword());

        if(userService.checkPassword(user,pwCheckForm.getPrePassword()))
        {
            userService.UserDelete(principal.getName());
            session.invalidate();
            return "redirect:/";
        }
        else {
            model.addAttribute("error",true);
            return "users/delete_check_pwd";
        }
    }


    // 아이디 찾기
    @GetMapping("/find-userId")
    public String findAccount(Model model,Principal principal) {

        if(principal != null)
        {
            return "redirect:/";
        }

        model.addAttribute("sendConfirm", false);
        model.addAttribute("error", false);
        return "users/userId_find";
    }


    @PostMapping("/find-userId")
    public String findAccount(Model model, @RequestParam(value="email")
            String email,Principal principal,HttpServletResponse httpServletResponse) {
        if(principal != null)
        {
            return "redirect:/";
        }

        try {
            UserResponseDTO users = this.userService.getUserEmailDTO(email);
            model.addAttribute("sendConfirm", true);
            model.addAttribute("userEmail", email);
            model.addAttribute("error", false);

            // 소셜로그인이란뜻
            if(users.getPassword() == null)
            {
                alertAndClose(httpServletResponse,"해당 유저는 소셜로그인 유저입니다.");
            }
            this.userService.sendfindIdEmail(email);

        } catch(DataNotFoundException e) {
            model.addAttribute("sendConfirm", false);
            model.addAttribute("error", true);
        }
        return "users/userId_find";
    }

    @GetMapping("/find-password")
    public String findPassword(Model model,Principal principal) {

        if(principal != null)
        {
            return "redirect:/";
        }

        model.addAttribute("sendConfirm", false);
        model.addAttribute("error", false);
        return "users/password_find";
    }


    @PostMapping("/find-password")
    public String findPassword(Model model, @RequestParam(value="email") String email,
                 @RequestParam(value="username") String username,Principal principal,HttpServletResponse httpServletResponse) {


        if(principal != null)
        {
            return "redirect:/";
        }

        try {
            UserResponseDTO users = this.userService.getUserEmailDTO(email);

            if(users.getPassword() == null)
            {
                alertAndClose(httpServletResponse,"해당 유저는 소셜로그인 유저입니다.");
            }

            if(username.equals(users.getUsername()) && users.getPassword() != null)
            {
                userService.send_find_Password_Email(email);
                model.addAttribute("sendConfirm", true);
                model.addAttribute("userEmail", email);
                model.addAttribute("error", false);
            }
            else {
                model.addAttribute("sendConfirm", false);
                model.addAttribute("error", true);
            }
        } catch(DataNotFoundException e) {
            model.addAttribute("sendConfirm", false);
            model.addAttribute("error", true);
        }
        return "users/password_find";
    }

    @PostMapping("/delete/OAuth2/google")
    public String Delete_OAuth2_Google(Principal principal,HttpSession httpSession)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());
        String accesToken;

        Optional<OAuth2AccesTokenData> oAuth2AccesTokenData =
                oAuth2AccesTokenDataRepository.findByUsername(userResponseDTO.getUsername());

        if(!oAuth2AccesTokenData.isEmpty())
        {
            accesToken = oAuth2AccesTokenData.get().getToken();
            userService.revokeGoogleToken(accesToken);
            deleteUserService.OAuth2Delete(userResponseDTO.getUsername());
            oAuth2AccesTokenDataRepository.delete(oAuth2AccesTokenData.get());
            httpSession.invalidate();
        }

        return "redirect:/";
    }

    @PostMapping("/delete/OAuth2/naver")
    public String Delete_OAuth2_Naver(Principal principal,HttpSession httpSession)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());
        String accesToken;

        Optional<OAuth2AccesTokenData> oAuth2AccesTokenData =
                oAuth2AccesTokenDataRepository.findByUsername(userResponseDTO.getUsername());

        if(!oAuth2AccesTokenData.isEmpty())
        {
            accesToken = oAuth2AccesTokenData.get().getToken();
            userService.NaverDelete(accesToken);
            deleteUserService.OAuth2Delete(userResponseDTO.getUsername());
            oAuth2AccesTokenDataRepository.delete(oAuth2AccesTokenData.get());
            httpSession.invalidate();
        }

        return "redirect:/";
    }



    public static void alertAndClose(HttpServletResponse response, String msg) {
        try {
            response.setContentType("text/html; charset=utf-8");
            PrintWriter w = response.getWriter();
            w.write("<script>alert('"+msg+"');history.go(-1);</script>");
            w.flush();
            w.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
