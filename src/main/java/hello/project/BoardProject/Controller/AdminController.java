package hello.project.BoardProject.Controller;


import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Board.Response.Board_Views_ResponseDTO;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.ChartData;
import hello.project.BoardProject.DTO.ImageUploadDTO;
import hello.project.BoardProject.DTO.Users.ImageResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.BoardCategory;
import hello.project.BoardProject.Form.Users.UserModifyForm;
import hello.project.BoardProject.Service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    private final BoardService boardService;
    private final ImageService imageService;
    private final CommentService commentService;
    private final LoginLogService loginLogService;
    private final Delete_UserService deleteUserService;

    /*
     관리자 메인 페이지
     */
    @GetMapping("")
    public String adminMainPage()
    {
        return "admin/admin_main";
    }

    // 가입한 유저의 리스트를 볼 수 있는 페이지
    @GetMapping("/user_list")
    public String UserList(Model model,@RequestParam(defaultValue = "0") int page)
    {
        Page<UserResponseDTO> users = userService.UserPageList(page);
        model.addAttribute("userList",users);
        return "admin/admin_user_list";
    }

    // 유저 상세 정보를 볼 수 있는 페이지(유저가 마이페이지 보는거랑 동일한 화면)
    @GetMapping("/user/{userId}")
    public String UserDetail(@PathVariable Long userId, Model model)
    {
        // 유저정보 가져오기
        UserResponseDTO userResponseDTO = this.userService.getUserDTO(userId);
        model.addAttribute("user",userResponseDTO);

        // 유저가 작성한 게시글 개수
        Long boardCount = boardService.getBoardCount(userResponseDTO);
        model.addAttribute("boardCount", boardCount);

        // 유저가 작성한 최근 5개 게시글 가져오기
        List<BoardResponseDTO> boardList = boardService.getBoardTop5LatestByUser(userResponseDTO);
        model.addAttribute("boardList", boardList);

        // 유저가 작성한 최근 5개 댓글 가져오기
        List<CommentResponseDTO> commentList = commentService.getCommentTop5LatestByUser(userResponseDTO);
        model.addAttribute("commentList",commentList);

        // 유저가 작성한 댓글 개수
        int commentCount = commentService.getCommentCount(userResponseDTO);
        model.addAttribute("commentCount",commentCount);

        return "admin/user_detail";
    }


    // 유저 삭제
    @GetMapping("/user_delete/{userId}")
    public String UserDelete(@PathVariable Long userId)
    {
        // 유저 정보 가져오기
        UserResponseDTO userResponseDTO = userService.getUserDTO(userId);

        // 유저 삭제
        userService.UserDelete(userResponseDTO.getUsername());

        return "redirect:/admin/user_list";
    }

    // 유저 데이터 수정
    @GetMapping("/user/info/{userId}")
    public String Admin_User_Modify(@PathVariable Long userId,Model model)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(userId);
        UserModifyForm userModifyForm = new UserModifyForm();
        userModifyForm.setId(userId);
        userModifyForm.setNickname(userResponseDTO.getNickname());
        model.addAttribute("userModifyForm",userModifyForm);
        ImageResponseDTO image = imageService.findImage(userResponseDTO.getEmail());
        model.addAttribute("image", image);
        return "admin/admin_profile";
    }



    // 유저 데이터 수정
    @PostMapping("/user/info/{userId}")
    public String usermodify(@PathVariable Long userId,
                             @Valid UserModifyForm userModifyForm,
                             BindingResult bindingResult, Model model,
                             @ModelAttribute ImageUploadDTO imageUploadDTO) {

        UserResponseDTO users = userService.getUserDTO(userId);
        ImageResponseDTO image = imageService.findImage(users.getEmail());


        if (bindingResult.hasErrors()) {
            userModifyForm.setId(userId);
            model.addAttribute("image", image); // 기존 이미지 안날라가게 Views로 이미지 데이터 전달
            return "admin/admin_profile";
        }

        // 닉네임은 수정을 안함(수정폼 닉네임 == 기존 닉네임)
        if(userModifyForm.getNickname().equals(users.getNickname()))
        {
            imageService.upload(imageUploadDTO,users.getEmail()); // 이미지 수정
            return "redirect:/admin/user/"+userId;
        }
        // 닉네임을 수정함
        else {
            // 여기서 중복된 닉네임 체크 (소프트 삭제된 유저의 닉네임도 체크해야함 )
            if(userService.checkNickname(userModifyForm.getNickname()) ||
                    deleteUserService.checkNickname(userModifyForm.getNickname()))
            {
                userModifyForm.setId(userId);
                model.addAttribute("image", image);
                bindingResult.reject("modify_Failed","중복된 닉네임입니다");
                return "admin/admin_profile";
            }
            else {
                imageService.upload(imageUploadDTO,users.getEmail());
                // 유저 닉네임 수정 후 저장
                userService.NicknameUpdate(userModifyForm.getNickname(),users.getUsername());
                return "redirect:/admin/user/"+userId;
            }
        }
    }

    // 유저가 작성한 게시글 리스트 보기
    @GetMapping("/list/ByBoard/{userId}/{CategoryName}")
    public String AdminUserListBoard(@PathVariable Long userId, @PathVariable String CategoryName,
                                Model model,
                                @RequestParam(defaultValue = "0") int page)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(userId); // 유저정보 가져오기

        int category = switch (CategoryName) {
            case "qna" -> BoardCategory.QNA.getStatus();
            case "free" -> BoardCategory.FREE.getStatus();
            case "bug" -> BoardCategory.TENDI.getStatus();
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        };

        String Categorys;

        if(category ==0)
        {
            Categorys = "질문 게시글";
        }
        else if(category == 1)
        {
            Categorys ="자유 게시글";
        }
        else
        {
            Categorys = "버그 게시글";
        }


        model.addAttribute("boardName",category);
        model.addAttribute("userId",userResponseDTO.getUsername());

        // 카테고리별 게시글 페이징 데이터 가져오기
        Page<BoardResponseDTO> paging = boardService.getPersonalBoardList(page, userResponseDTO.getUsername(),category);
        model.addAttribute("user", userResponseDTO);
        model.addAttribute("paging", paging);
        // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
        model.addAttribute("type", "총 "+Categorys+" 개수");
        return "admin/admin_personal_list";
    }

    // 유저가 작성한 댓글 페이징보기
    @GetMapping("/list/ByComment/{userId}")
    public String UserListBoard(@PathVariable Long userId,
                                Model model,
                                @RequestParam(defaultValue = "0") int page)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(userId);


        Page<CommentResponseDTO> paging = commentService.getPersonalCommentList(page, userResponseDTO.getUsername());
        model.addAttribute("user", userResponseDTO);
        model.addAttribute("paging", paging);
        model.addAttribute("type","총 댓글 개수");
        // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
        return "comment/personal_list";
    }

    /*
       날짜별 조회수 차트
       일단 삭제된 게시글의 조회수도 추가되게끔 하였다
     */
    @GetMapping("/board/views/chart")
    public String Board_Views_Chart(Model model)
    {
        for(int i=0; i<=2; i++)
        {
            ChartData chartData = boardService.BoardViewChart(i);
            if(i==0)
            {
                model.addAttribute("Board_Views_Data_qna" , chartData);
            }
            if(i==1)
            {
                model.addAttribute("Board_Views_Data_free" , chartData);
            }
            if(i==2)
            {
                model.addAttribute("Board_Views_Data_bug" ,chartData);
            }
        }

        return "admin/chart/board/views_chart";
    }

    /*
      날짜별 접속로그 차트
      동일 날짜에 다수 접속한 유저는 제한이 필요함(코드가 복잡해져서 따로 구현X)
     */
    @GetMapping("/users/log/chart")
    public String LoginLogChart(Model model)
    {
        ChartData ChartData = loginLogService.LoginLogChartData();
        model.addAttribute("LoginLog",ChartData);

        return "admin/chart/users/user_log_chart";
    }

    /*
     날짜별 데이터 생성 조회
    */
    @GetMapping("/board/create/chart")
    public String showChart(Model model) {

        for(int i =0; i<=2; i++)
        {
            ChartData ChartBoard = boardService.BoardChart(i);

            if(i == 0)
            {
                model.addAttribute("chartData", ChartBoard);
            }

            if(i == 1)
            {
                model.addAttribute("FreeData" , ChartBoard);
            }

            if(i == 2)
            {
                model.addAttribute("BugData" , ChartBoard);
            }
        }

        return "admin/chart/board/board_chart"; // 템플릿 이름
    }

    /*
     회원가입 일자 차트
     */
    @GetMapping("/users/register/chart")
    public String RegisterChart(Model model)
    {
        ChartData chartData =  userService.RegisterChart();
        model.addAttribute("chartData", chartData);

        return "admin/chart/users/chart";
    }

    /*
     회원탈퇴 차트(소프트 삭제)
     */
    @GetMapping("/users/delete-chart")
    public String DeleteChart(Model model)
    {
        ChartData chartData = deleteUserService.DeleteChart();
        model.addAttribute("chartData", chartData);

        return "admin/chart/users/delete_chart";
    }

    /*
     댓글 생성 차트
     */
    @GetMapping("/comment/create/chart")
    public String CommentChart(Model model)
    {
        for(int i=0; i<=2; i++)
        {
            // 카테고리 0,1,2의 게시글 리스트를 가져옴
            List<BoardResponseDTO> boardResponseDTOList = boardService.BoardCategoryList(i);

            ChartData commentChartData = commentService.commentChart(boardResponseDTOList);
            if(i==0)
            {
                model.addAttribute("commentChartData_qna" , commentChartData);
            }
            if(i==1)
            {
                model.addAttribute("commentChartData_free" , commentChartData);
            }
            if(i==2)
            {

                model.addAttribute("commentChartData_bug" , commentChartData);
            }
        }

        return "admin/chart/comment/chart";
    }

}

