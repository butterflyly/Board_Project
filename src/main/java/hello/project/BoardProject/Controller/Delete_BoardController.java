package hello.project.BoardProject.Controller;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.BoardCategory;
import hello.project.BoardProject.Entity.Board.BoardImage;
import hello.project.BoardProject.Entity.Board.BoardPageNumber;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Form.Board.BoardSearchCondition;
import hello.project.BoardProject.Form.CommentForm;
import hello.project.BoardProject.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin/deleteBoard")
public class Delete_BoardController {

    private final Delete_BoardService delete_boardService;
    private final HiddenCommentService hiddenCommentService;
    private final HiddenBoardImageService hiddenBoardImageService;


    /*
      - 삭제 게시글 상세 조회(R)
      - 현재 Comment 관련 문제있음
     */
    @GetMapping("/detail/{id}")
    public String BoardDetail(@PathVariable Long id, Model model,
                              @RequestParam(defaultValue = "0") int page
            , @ModelAttribute CommentForm commentForm, Principal principal)
    {

        BoardResponseDTO board = delete_boardService.detail(id);
        Page<CommentResponseDTO> commentPaging = hiddenCommentService.findAll(page,board.getId());


        List<BoardImage> boardImages = hiddenBoardImageService.ImageList(board.getId());
        List<String> ImageUrls = boardImages.stream()
            .map(BoardImage::getUrl).collect(Collectors.toList());

        model.addAttribute("boardCommentPaging", commentPaging);
        model.addAttribute("totalCount", commentPaging.getTotalElements());
        model.addAttribute("board",board);
        model.addAttribute("commentForm",commentForm);
        model.addAttribute("boardImages", ImageUrls);

        /*이전글다음글번호와 제목을 html에서 불러올수있게 model.addAttribute() 작성*/
        BoardResponseDTO predto = delete_boardService.getPrePage(board);
        BoardResponseDTO nextdto = delete_boardService.getNextPage(board);

        // 원래는 PREDTO로 통일을 하고 HTML에서 하는 것이 코드 간결화에 더 도움되나
        // 기존에 이렇게 사용하여 일단 이렇게 ㅠㅠ
        model.addAttribute("predto",predto);
        model.addAttribute("nextdto",nextdto);


        return "admin/delete_board/delete_board_detail";
    }

    @GetMapping("/list/{CategoryName}")
    public String BoardList(@PathVariable String CategoryName, Model model
            , @RequestParam(value = "kw", defaultValue = "") String kw
            , @RequestParam(value = "type",defaultValue = "") String type,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "create_Date") String sort,
                            BoardSearchCondition boardSearchCondition)
    {
        int category = switch (CategoryName) {
            case "qna" -> BoardCategory.QNA.getStatus();
            case "free" -> BoardCategory.FREE.getStatus();
            case "bug" -> BoardCategory.TENDI.getStatus();
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        };

        boardSearchCondition.setType(type);
        String search  = boardSearchCondition.getType();

        log.info("SEARCH : " + search);

        model.addAttribute("search",search);
        model.addAttribute("boardName",category);
        Page<BoardResponseDTO> paging = delete_boardService.getPage(page,sort,category,search,kw);
        model.addAttribute("paging",paging);

        model.addAttribute("kw",kw);
        model.addAttribute("sort", sort); // 정렬 정보 유지
        model.addAttribute("CategoryName",CategoryName);


        return "admin/delete_board/delete_board_list";
    }

    /*
      게시글 완전삭제
     */
    @GetMapping("/delete/{id}")
    public String Board_HardDelete(@PathVariable Long id)
    {
        BoardResponseDTO boardResponseDTO = delete_boardService.detail(id);

        String categoryName =null;
        // 삭제 하기전에 리다이렉트로 사용할 변수 미리 가져오기
        switch (boardResponseDTO.getCategory())
        {
            case 0 -> categoryName = "qna";
            case 1 -> categoryName = "free";
            case 2 -> categoryName = "bug";
        }

        delete_boardService.Board_HardDelete(id);
        return "redirect:/admin/deleteBoard/list/"+categoryName;
    }
}
