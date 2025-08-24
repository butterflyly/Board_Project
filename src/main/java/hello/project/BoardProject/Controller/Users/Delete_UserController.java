package hello.project.BoardProject.Controller.Users;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.BoardCategory;
import hello.project.BoardProject.Form.Users.UserModifyForm;
import hello.project.BoardProject.Service.Board.BoardService;
import hello.project.BoardProject.Service.Comment.CommentService;
import hello.project.BoardProject.Service.Users.Delete_UserService;
import hello.project.BoardProject.Service.Users.ImageService;
import hello.project.BoardProject.Service.Users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin/deleteUsers")
/*
  SOFT DELETE 유저 관리 컨트롤러
 */
public class Delete_UserController {

    private final Delete_UserService deleteUserService;
    private final UserService userService;
    private final ImageService imageService;
    private final BoardService boardService;
    private final CommentService commentService;


    // 탈퇴한 유저 리스트
    @GetMapping("/user_list")
    public String UserList(Model model,   @RequestParam(defaultValue = "0") int page)
    {
        // 삭제 유저 데이터 페이징 데이터 가져오기
        Page<UserResponseDTO> users = deleteUserService.UserList(page);
        model.addAttribute("userList",users);
        return "admin/delete_users/admin_user_list";
    }


    // 유저 상세 정보를 볼 수 있는 페이지(유저가 마이페이지 보는거랑 동일한 화면)
    @GetMapping("/user/{userId}")
    public String UserDetail(@PathVariable Long userId, Model model)
    {
        UserResponseDTO userResponseDTO = this.deleteUserService.getUserDTO(userId);
        model.addAttribute("user",userResponseDTO);

        // 삭제한 유저가 작성한 게시글 개수 데이터
        Long boardCount = boardService.getDeleteUserBoardCount(userResponseDTO);
        model.addAttribute("boardCount", boardCount);

        // 삭제한 유저가 작성한 게시글 최근 5개 데이터
        List<BoardResponseDTO> boardList =
                boardService.getDeleteUserBoardTop5LatestByUser(userResponseDTO);
        model.addAttribute("boardList", boardList);

        // 삭제한 유저가 작성한 댓글 최근 5개 데이터
        List<CommentResponseDTO> commentList =
                commentService.getDeleteUserCommentTop5LatestByUser(userResponseDTO);
        model.addAttribute("commentList",commentList);

        // 삭제한 유저가 작성한 댓글 개수 데이터
        Long commentCount = commentService.getDeleteUserCommentCount(userResponseDTO);
        model.addAttribute("commentCount",commentCount);

        return "admin/delete_users/user_detail";
    }


    // SOFT DELETE 유저가 작성한 게시글 리스트 보기
    @GetMapping("/list/ByBoard/{userId}/{CategoryName}")
    public String DeleteUserListBoard(@PathVariable Long userId, @PathVariable String CategoryName,
                                     Model model,
                                     @RequestParam(defaultValue = "0") int page)
    {
        UserResponseDTO userResponseDTO = deleteUserService.getUserDTO(userId);

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

        Page<BoardResponseDTO> paging =
                deleteUserService.getPersonalBoardList(page, userResponseDTO.getUsername(),category);
        model.addAttribute("user", userResponseDTO);
        model.addAttribute("paging", paging);
        // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
        model.addAttribute("type", "총 "+Categorys+" 개수");
        return "admin/delete_users/admin_personal_list";
    }

    // SOFT DELETE 유저가 작성한 댓글 리스트 보기
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list/ByComment/{userId}")
    public String DeleteUserListComment(@PathVariable Long userId,
                                Model model,
                                @RequestParam(defaultValue = "0") int page)
    {

        UserResponseDTO userResponseDTO = deleteUserService.getUserDTO(userId);

        Page<CommentResponseDTO> paging = deleteUserService.getPersonalCommentList(page, userResponseDTO.getUsername());
        model.addAttribute("user", userResponseDTO);
        model.addAttribute("paging", paging);
        model.addAttribute("type","총 댓글 개수");
        // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
        return "comment/personal_list";
    }

    // 유저 HARD DELETE
    @GetMapping("/user_delete/{userId}")
    public String UserDelete(@PathVariable Long userId)
    {
        UserResponseDTO userResponseDTO = deleteUserService.getUserDTO(userId);
        deleteUserService.Hard_Delete(userResponseDTO.getId());

        return "redirect:/admin/deleteUsers/user_list";
    }

    // SOFT DELETE 유저 데이터 수정
    @GetMapping("/user/info/{id}")
    public String Admin_User_Modify(@PathVariable("id") Long id, Model model)
    {

        UserResponseDTO userResponseDTO = deleteUserService.getUserDTO(id);
        UserModifyForm userModifyForm = new UserModifyForm();

        userModifyForm.setId(userResponseDTO.getId());

        userModifyForm.setNickname(userResponseDTO.getNickname());
        model.addAttribute("userModifyForm",userModifyForm);
        return "admin/delete_users/deleteUser_profile";
    }

    // SOFT DELETE 유저 데이터 수정
    @PostMapping("/user/info/{id}")
    public String usermodify(@PathVariable("id") Long id,
                             @Valid UserModifyForm userModifyForm,
                             BindingResult bindingResult,Model model) {

        UserResponseDTO users = deleteUserService.getUserDTO(id);


        if (bindingResult.hasErrors()) {
            userModifyForm.setId(users.getId());
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "admin/delete_users/deleteUser_profile";
        }

        // 기존 계정 닉네임이랑 변경하려는 닉네임이 일치할 경우
        if(userModifyForm.getNickname().equals(users.getNickname()))
        {
            return "redirect:/admin/deleteUsers/user/"+id;
        }

        // 닉네임이 중복된 경우
        if(deleteUserService.checkNickname(userModifyForm.getNickname()) ||
                userService.checkNickname(userModifyForm.getNickname()))
        {
            userModifyForm.setId(users.getId());
            bindingResult.reject("modify_Failed","중복된 닉네임입니다");
            return "admin/delete_users/deleteUser_profile";
        }
        // 닉네임이 중복되지 않은 경우
        else {
            deleteUserService.deleteUserInfo(users,userModifyForm.getNickname());
            return "redirect:/admin/deleteUsers/user/"+id;
        }

    }

    // SOFT DELETE 유저 데이터 복원
    @PostMapping("/ReStore/{id}")
    public String UserReStore(@PathVariable Long id)
    {
        UserResponseDTO userResponseDTO = deleteUserService.getUserDTO(id);

        deleteUserService.User_ReStore(id); // 유저 복원
        imageService.ImageReStore(userResponseDTO.getEmail()); // 유저 이미지 복원
        return "redirect:/admin/deleteUsers/user_list";
    }

}
