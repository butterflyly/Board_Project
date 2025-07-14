package hello.project.BoardProject.Controller;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Form.CommentForm;
import hello.project.BoardProject.Repository.Comment.CommentImageRepository;
import hello.project.BoardProject.Service.BoardService;
import hello.project.BoardProject.Service.CommentService;
import hello.project.BoardProject.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.security.Principal;
import java.util.Objects;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final BoardService boardService;
    private final CommentImageRepository commentImageRepository;
    private final int PAGESIZE = 10;

    @Value("${file.commentImagePath}")
    private String uploadFolder;


    // 댓글 작성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{boardId}")
    @ResponseBody
    public void writeReply(@PathVariable Long boardId,
            @ModelAttribute CommentForm commentForm,
                           @RequestParam(value = "file", required = false) MultipartFile file,
                           Principal principal) {

        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName()); // 유저정보 가져오기
        String content = commentForm.getContent(); // 댓글 내용 가져오기
        boolean secret = commentForm.getSecret(); // 댓글 비밀 관련
        commentService.writeReply(userResponseDTO, boardId, content,secret, file);
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reply/create")
    @ResponseBody
    public String replyCreate(Model model, @ModelAttribute CommentForm commentForm,
                              @RequestParam(value = "reply-comment-file", required = false) MultipartFile file,
                              Principal principal) {


        // 부모 댓글값 가져오기
        CommentResponseDTO commentResponseDTOParent = commentService.getCommentDTO(commentForm.getParentId());

        // 부모댓글이 없으면 자식댓글이 나올 수 없으므로 false
        if (commentResponseDTOParent == null) {
            return "Search_Fail";
        }

        // 게시글 가져오기
        BoardResponseDTO boardResponseDto = boardService.detail(commentResponseDTOParent.getBoard().getId());

        // 게시글 작성자 유저아이디 변수 가져오기
        String boardWriterUsername = boardResponseDto.getUsername();

        UserResponseDTO user = userService.getUserDTO(principal.getName());

        log.info("commentId :" +commentForm.getCommentId());
        log.info("parentId :"+commentForm.getParentId());

        //
        int booleanCheck = commentService.childListCheck(commentResponseDTOParent);


        // 대댓글이 없고 댓글이 비밀상태이면 게시글 작성자,댓글작성자,관리자만 댓글을 달게 하고 그 댓글들은 모두 비밀댓글로
        if(commentResponseDTOParent.getChildren() == null && commentResponseDTOParent.getSecret())
        {
            if(Objects.equals(commentResponseDTOParent.getUsers().getUsername(), user.getUsername()) // 댓글 작성자와 대댓글을 작성하려는 유저가 같은가
                    ||
                    Objects.equals(boardWriterUsername, user.getUsername()) // 대댓글을 작성하려는 유저가 게시글 작성자인가
             || user.getUsername().equals("admin")) // 대댓글을 달려는 유저가 관리자인가
            {
                // 모두 비밀댓글로 만들기
                commentService.createReplyComment(commentForm.getContent(),
                       true, user, boardResponseDto, commentResponseDTOParent,file);
                return "Success";
            }
            else {
                return "Failed";
            }
        }

        // 대댓글이 있는데 대댓글이 모두 비밀댓글이고 부모댓글인 본인도 비밀댓글인 경우
        if(commentResponseDTOParent.getChildren() !=null && commentResponseDTOParent.getSecret())
        {
            if(booleanCheck ==0)
            {
                if(Objects.equals(commentResponseDTOParent.getUsers().getUsername(), user.getUsername()) // 댓글 작성자와 대댓글을 작성하려는 유저가 같은가
                        ||
                        Objects.equals(boardWriterUsername, user.getUsername()) // 대댓글을 작성하려는 유저가 게시글 작성자인가
                        || user.getUsername().equals("admin")) // 대댓글을 달려는 유저가 관리자인가
                {
                    // 모두 비밀댓글로 만들기
                    commentService.createReplyComment(commentForm.getContent(),
                            true, user, boardResponseDto, commentResponseDTOParent,file);
                    return "Success";
                }
                else {
                    return "Failed";
                }
            }
            else { // 비밀댓글이 아닌 대댓글이 있다.
                commentService.createReplyComment(commentForm.getContent(),
                        commentForm.getSecret(), user, boardResponseDto, commentResponseDTOParent,file);
                return "Success";
            }
        }

        // 본인이 비밀댓글이 아닌 경우
        if(!commentResponseDTOParent.getSecret())
        {
            // 그냥 대댓글 생성하면 됨 
            commentService.createReplyComment(commentForm.getContent(),
                    commentForm.getSecret(), user, boardResponseDto, commentResponseDTOParent,file);
            return "Success";
        }

        return "Success";
    }


    /**
     * 댓글 수정
     * @param commentForm
     * @param principal
     */

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{commentId}")
    @ResponseBody
    public String updateComment(@ModelAttribute CommentForm commentForm,
                                @RequestParam(value = "comment-update-file", required = false) MultipartFile file,
                                @PathVariable Long commentId, Principal principal,HttpServletResponse httpServletResponse) {

        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());

        if(commentForm.getCommentUsers() != null)
        {
            if(!(userResponseDTO.getId().equals(commentForm.getCommentUsers())))
            {
                if(!(userResponseDTO.getUsername().equals("admin")))
                {
                    alertAndClose(httpServletResponse,"수정 권한이 없습니다");
                }
            }
        }
        // 댓글 작성 유저가 없는 경우
        else {
            if(!(userResponseDTO.getUsername().equals("admin")))
            {
                alertAndClose(httpServletResponse,"수정 권한이 없습니다");
            }
        }

        if(commentForm.getCommentId() == null)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글이 없습니다.");
        }

        // 대댓글(답글)도 id로 찾을 수 있음(댓글과 동일 객체 사용이 가능하니 하나의 메서드로 처리 가능)
        CommentResponseDTO comment = commentService.getCommentDTO(commentId);
        commentService.childListCheck(comment);
        // 댓글 내용, 비밀 댓글 여부만 수정 할테니 해당 값 넘기기
        commentService.modify(comment, commentForm.getContent().trim(),commentForm.getSecret(),file);

        return "UpdateSuccess";

    }

    // 댓글 수정중 이미지 삭제
    @PostMapping("/Image/delete/{imageId}/{commentId}")
    @ResponseBody
    public void ImageDelete(@PathVariable Long imageId, @PathVariable Long commentId)
    {
        commentService.ImageDelete(commentId);
    }

    // 댓글 삭제 메서드
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{commentId}")
    @ResponseBody
    public void delete(@PathVariable Long commentId, Principal principal,HttpServletResponse httpServletResponse) {

        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());
        CommentResponseDTO commentResponseDTO = commentService.getCommentDTO(commentId);

        // 댓글 작성 유저가 있는 경우
        if(commentResponseDTO.getUsers() != null)
        {
            if(!(userResponseDTO.getUsername().equals(commentResponseDTO.getUsername())))
            {
                if(!(userResponseDTO.getUsername().equals("admin")))
                {
                    alertAndClose(httpServletResponse,"삭제 권한이 없습니다");
                }
            }
        }

        // 댓글 작성 유저가 없는 경우
        else {
            if(!(userResponseDTO.getUsername().equals("admin")))
            {
                alertAndClose(httpServletResponse,"삭제 권한이 없습니다");
            }
        }

        if(commentResponseDTO == null)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글이 없습니다.");
        }

        commentService.delete(commentId);
    }

    // 내 댓글 목록보기
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list/ByComment/{userId}")
    public String UserListBoard(@PathVariable Long userId,
                                Model model,
                                @RequestParam(defaultValue = "0") int page,Principal principal)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());

        if (userResponseDTO.getId() != userId) {

            if(!Objects.equals(principal.getName(), "admin"))
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
        }

        Page<CommentResponseDTO> paging = commentService.getPersonalCommentList(page, userResponseDTO.getUsername());
        model.addAttribute("user", userResponseDTO);
        model.addAttribute("paging", paging);
        model.addAttribute("type","총 댓글 개수");
        // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
        return "comment/personal_list";
    }

    @PostMapping("/{comment_Id}/recommend")
    public String recommend(@PathVariable("comment_Id") Long comment_Id,
                            Principal principal,HttpServletResponse httpServletResponse,Model model) {
        CommentResponseDTO commentResponseDTO = commentService.getCommentDTO(comment_Id);
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());

        boolean check = commentService.voteUsers(commentResponseDTO,userResponseDTO); // 유저가 댓글 추천을 했는가
        boolean Not_Recommend_check = commentService.not_voteUsers(commentResponseDTO,userResponseDTO);


        if(check) // 유저가 댓글 추천을 이미 한 경우
        {
            if(Not_Recommend_check) // 유저가 댓글을 이미 비추천한 경우(사실 없는 경우임)
            {
                // 어차피 비추천 했으므로 추천이 취소되어도 됨
                commentService.cancelRecommend(comment_Id, principal.getName());
                model.addAttribute("message","추천을 취소했어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
            }
            else { // 유저가 댓글을 비추천하지 않은 경우
                //비추천을 한게 아니므로 추천을 취소할 수 있음
                commentService.cancelRecommend(comment_Id, principal.getName());
                model.addAttribute("message","추천을 취소했어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
            }
            model.addAttribute("searchUrl","/board/detail/"+commentResponseDTO.getBoard().getId());
            return "message";
        }
        else { // 유저가 추천을 안한 경우
            if(Not_Recommend_check) // 유저가 이미 비추천을 했음 => 추천을 할 수 없음
            {
                model.addAttribute("message","비추천을 해서 추천할수 없어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
                model.addAttribute("searchUrl","/board/detail/"+commentResponseDTO.getBoard().getId());
            }
            else { // 비추천을 안한 경우
                // 추천을 할 수 있음
                commentService.recommend(comment_Id, principal.getName());
                model.addAttribute("message","추천했어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
                model.addAttribute("searchUrl","/board/detail/"+commentResponseDTO.getBoard().getId());
            }

            return "message";
        }
    }

    @PostMapping("/{comment_Id}/notrecommend")
    public String Not_recommend(@PathVariable("comment_Id") Long comment_Id,
                                Principal principal, HttpServletResponse httpServletResponse, Model model) {
        CommentResponseDTO commentResponseDTO = commentService.getCommentDTO(comment_Id);
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());

        boolean Recommend_check = commentService.voteUsers(commentResponseDTO,userResponseDTO);
        boolean Not_Recommend_check = commentService.not_voteUsers(commentResponseDTO,userResponseDTO);

        if(Not_Recommend_check) // 유저가 이미 비추천을 한 경우
        {
            if(Recommend_check) // 유저가 댓글을 이미 추천한 경우(사실 없는 경우임)
            {
                // 어차피 추천 했으므로 비추천이 취소되어도 됨
                commentService.not_cancelRecommend(comment_Id, principal.getName());
                model.addAttribute("message","비추천을 취소했어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
            }
            else { // 유저가 댓글을 비추천하지 않은 경우
                //비추천을 한게 아니므로 추천을 취소할 수 있음
                commentService.not_cancelRecommend(comment_Id, principal.getName());
                model.addAttribute("message","비추천을 취소했어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
            }
            model.addAttribute("searchUrl","/board/detail/"+commentResponseDTO.getBoard().getId());
            return "message";
        }
        else { // 유저가 추천을 안한 경우
            if(Recommend_check) // 유저가 이미 추천을 했음 => 비추천을 할 수 없음
            {
                model.addAttribute("message","추천을 해서 비추천할 수 없어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
                model.addAttribute("searchUrl","/board/detail/"+commentResponseDTO.getBoard().getId());
            }
            else { // 추천을 안한 경우
                // 비추천을 할 수 있음
                commentService.not_recommend(comment_Id, principal.getName());
                model.addAttribute("message","비추천했어양");
                log.info("댓글 추천 개수 :" + commentResponseDTO.getRecommends().size());
                model.addAttribute("searchUrl","/board/detail/"+commentResponseDTO.getBoard().getId());
            }
            return "message";
        }
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
