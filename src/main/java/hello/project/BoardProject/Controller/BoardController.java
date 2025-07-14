package hello.project.BoardProject.Controller;


import hello.project.BoardProject.DTO.Board.BoardImageUploadDTO;
import hello.project.BoardProject.DTO.Board.Response.BoardNotVotorResponseDTO;
import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Board.Response.BoardVotorResponseDTO;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.*;
import hello.project.BoardProject.Form.Board.BoardCreateForm;
import hello.project.BoardProject.Form.Board.BoardSearchCondition;
import hello.project.BoardProject.Form.CommentForm;
import hello.project.BoardProject.Form.UploadForm;
import hello.project.BoardProject.Service.BoardService;
import hello.project.BoardProject.Service.CommentService;
import hello.project.BoardProject.Service.Delete_UserService;
import hello.project.BoardProject.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor // 생성자를 만들어주는 어노테이션
@RequestMapping("/board")
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final UserService userService;
    private final Delete_UserService deleteUserService;

    /*
     게시글 등록 GetMapping(C)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write/{CategoryName}")
    public String BoardWrite(@PathVariable String CategoryName,BoardImageUploadDTO boardImageUploadDTO,
                             Model model,Principal principal,HttpSession httpSession)
    {
        // 카테고리 이름에 맞게끔 변수지정
        int category = switch (CategoryName) {
            case "qna" -> BoardCategory.QNA.getStatus(); // BoardCategory QNA(0)
            case "free" -> BoardCategory.FREE.getStatus(); // BoardCategory FREE(1)
            case "bug" -> BoardCategory.TENDI.getStatus(); // BoardCategory TENDI(2)
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        };

        // 생성 폼 불러오기
        BoardCreateForm boardCreateForm = new BoardCreateForm();

        // 작성하는 유저 데이터 가져오기
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());

        // 유저 닉네임 생성폼에 적용
        boardCreateForm.setNickname(userResponseDTO.getNickname());

        model.addAttribute("boardName",category); // 현재 작성하는 게시글 카테고리를 확인하기 위함
        model.addAttribute("CategoryName",CategoryName); // @PathVariable 변수 지정 위의 변수와 통일할 수 있을텐데 쉬운길을 선택함
        model.addAttribute("boardCreateForm",boardCreateForm);
        return "board/write";
    }


    /*
    게시글 등록 PostMapping(C)
    */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write/{CategoryName}")
    public String BoardWrite(@PathVariable String CategoryName,
                             @ModelAttribute // 사용자가 요청시 전달하는 값을 오브젝트 형태로 매핑해주는 어노테이션
                                  BoardImageUploadDTO boardImageUploadDTO,
                             @RequestParam("files") List<MultipartFile> files, HttpSession session,
                             @ModelAttribute("boardCreateForm") @Valid BoardCreateForm boardCreateForm,
                             BindingResult bindingResult, Principal principal)
    {
        int category = switch (CategoryName) {
            case "qna" -> BoardCategory.QNA.getStatus();
            case "free" -> BoardCategory.FREE.getStatus();
            case "bug" -> BoardCategory.TENDI.getStatus();
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        };

        // BoardCreateForm에서 설정한 조건을 충족하지 못하면 발생하는 에러
        if(bindingResult.hasErrors())
        {
            return "board/write";
        }

        String username = principal.getName();

        boardService.save(boardCreateForm.getTitle(), boardCreateForm.getContent(),boardCreateForm.isFix(),
                username,category,boardCreateForm.getFiles());
        return "redirect:/board/list/"+CategoryName;
    }

    /*
     게시글 조회(R)
     */
    @GetMapping("/detail/{id}")
    public String BoardDetail(@PathVariable Long id, Model model,
                              @RequestParam(defaultValue = "0") int page
                             , HttpServletRequest request,
                              HttpServletResponse response
                               , Principal principal
    ,@RequestParam(defaultValue = "createDate") String sort)
    {

        BoardResponseDTO board = boardService.detail(id);  // DTO 데이터 가져오기

        List<String> VoterNickname = boardService.Board_Voter_Nickname_list(board);
        List<String> NotVoterNickname = boardService.Board_Not_Voter_Nickname_list(board);

        Page<CommentResponseDTO> commentPaging = commentService.findAll(page,board,sort); // 게시글 내 댓글 가져오기
        List<CommentResponseDTO> commentResponseDTOPage = commentService.CommentTop3(board); // 인기순 탑 3 댓글

        model.addAttribute("sort",sort); // 댓글 최신,추천순 sort
        model.addAttribute("totalCount", commentPaging.getTotalElements());
        model.addAttribute("boardCommentPaging", commentPaging); // 이게 제대로 안된단뜻이겠지

        model.addAttribute("board",board); // 게시글 정보
        model.addAttribute("commentTop",commentResponseDTOPage); // 댓글 탑3 리스트
        model.addAttribute("voterNickname",VoterNickname); // 추천 닉네임리스트
        model.addAttribute("notvoterNickname",NotVoterNickname); // 비추천 닉네임 리스트

        // 유저가 접속한 경우 읽은 게시글 읽음처리
        if(principal != null)
        {
            boardService.markBoardAsRead(board.getId(),principal.getName());
        }

        /* 조회수 로직 */
        this.boardService.updateViews(id, request, response,board);

        /*이전글다음글번호와 제목을 html에서 불러올수있게 model.addAttribute() 작성*/
     //   BoardPageNumber boardPageNumber = boardService.getBoardByPageId(board);

        BoardResponseDTO predto = boardService.getPrePage(board);
        BoardResponseDTO nextdto = boardService.getNextPage(board);

        // 원래는 PREDTO로 통일을 하고 HTML에서 하는 것이 코드 간결화에 더 도움되나
        // 기존에 이렇게 사용하여 일단 이렇게 ㅠㅠ
        model.addAttribute("predto",predto);
        model.addAttribute("nextdto",nextdto);


        // 게시글 추천 불리언 값
        if(principal != null && principal.getName() !=null)
        {
            UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());
            boolean hasBoardVoted = boardService.voteUsers(board,userResponseDTO);
            boolean hasBoardNotVoted = boardService.notvoteUsers(board,userResponseDTO);

            model.addAttribute("hasBoardVoted",hasBoardVoted);
            model.addAttribute("hasBoardNotVoted",hasBoardNotVoted);
        }
        else {
            model.addAttribute("hasBoardVoted", false);
            model.addAttribute("hasBoardNotVoted",false);
        }

        return "board/detail";
    }


    // 메인 리스트 페이지
    @GetMapping("/mainList")
    public String MainList(Model model)
    {
        // 종류가 많아지면 그냥 반복문으로 하기
        model.addAttribute("boardHotList",boardService.getMainViewsList());
        model.addAttribute("boardVoterList",boardService.getMainVoterList());
        model.addAttribute("boardHotList1",boardService.getMainViewsList(0));
        model.addAttribute("boardVoterList1",boardService.getMainVoterList(0));
        model.addAttribute("boardHotList2",boardService.getMainViewsList(1));
        model.addAttribute("boardVoterList2",boardService.getMainVoterList(1));
        model.addAttribute("boardHotList3",boardService.getMainViewsList(2));
        model.addAttribute("boardVoterList3",boardService.getMainVoterList(2));

        model.addAttribute("boardList",boardService.getMainListAll());
        model.addAttribute("boardList2",boardService.getMainList(0));
        model.addAttribute("boardList3",boardService.getMainList(1));
        model.addAttribute("boardList4",boardService.getMainList(2));

        return "Board/main_list";
    }




    /*
     게시글 목록 조회(R)
     */
    @GetMapping("/list/{CategoryName}")
    public String BoardList(@PathVariable String CategoryName, Model model
            , @RequestParam(value = "kw", defaultValue = "") String kw // 검색
            , @RequestParam(value = "type",defaultValue = "") String type, // 검색 타입
                            @RequestParam(defaultValue = "0") int page,
                            Principal principal,
                            @RequestParam(defaultValue = "createDate") String sort,
                            BoardSearchCondition boardSearchCondition
            )
    {

        int category = switch (CategoryName) {
            case "qna" -> BoardCategory.QNA.getStatus();
            case "free" -> BoardCategory.FREE.getStatus();
            case "bug" -> BoardCategory.TENDI.getStatus();
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        };

        boardSearchCondition.setType(type); // 검색 타입 주입
        String search  = boardSearchCondition.getType();

        log.info("SEARCH : " + search);


        model.addAttribute("search",search); // 검색타입
        model.addAttribute("boardName",category); // 카테고리

        List<BoardResponseDTO> boardFixList = boardService.FixList(category); // 고정게시글 리스트
        model.addAttribute("fixedPosters",boardFixList);
        model.addAttribute("CategoryName",CategoryName);

        Page<BoardResponseDTO> paging;

        paging = boardService.getPage(page,sort,category,search,kw);

        model.addAttribute("paging", paging);

        List<Long> ReadBoardId;

        // 사용자별 읽음 여부 테이블 조회
        if (principal != null) {
            // 현재 사용자가 조회한 게시글 목록 가져오기 (조회 기록 데이터베이스에서)
            ReadBoardId = boardService.ReadBoardIdList(principal.getName());
            UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());
            model.addAttribute("connection_nickname",userResponseDTO.getNickname()); // 메세지 링크관련하여 넣음
        }
        else {
            ReadBoardId = Collections.singletonList(0L);
        }

        model.addAttribute("CategoryName",CategoryName);
        model.addAttribute("kw",kw);
        model.addAttribute("sort", sort); // 정렬 정보 유지
        model.addAttribute("viewedBoardIds", ReadBoardId);

        return "board/list";
    }

    /*
     게시글 수정(U)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String BoardModify(@PathVariable Long id,Model model,
                              Principal principal,HttpServletResponse httpServletResponse)
    {

        BoardResponseDTO board = boardService.detail(id); /* 게시글 정보 가져오기 */

        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName()); // 로그인한 유저 정보 가져오기

        switch (board.getCategory())
        {
            case 0 -> model.addAttribute("boardName","질문과 답변 수정");
            case 1 -> model.addAttribute("boardName","자유게시판 수정");
            case 2 -> model.addAttribute("boardName","건의게시판 수정");
        }

        // case 1. 관리자 => 수정 가능
        // case 2. 게시글 작성유저 => 수정가능
        // case 3. 유저가 탈퇴한 게시글 관리자 => 수정가능
        // case 4. 유저가 탈퇴했고 관리자도 아님  => 수정 불가
        // case 5. 게시글 작성자가 아니고 관리자도 아님 => 수정 불가능

        // 게시글 유저가 삭제된 경우
        if(board.getUsers() == null)
        {
            // case 3
            if(principal.getName().equals("admin"))
            {
                BoardCreateForm boardCreateForm = new BoardCreateForm();
                boardCreateForm.setId(id);
                boardCreateForm.setTitle(board.getTitle());
                boardCreateForm.setContent(board.getContent());
                boardCreateForm.setNickname(userResponseDTO.getNickname());
                boardCreateForm.setFix(board.isFix());
                model.addAttribute("boardCreateForm",boardCreateForm);
                model.addAttribute("Image",board.getBoardImages());
                return "board/modify";
            }
            // case 4
            else {
                alertAndClose(httpServletResponse,"수정 권한이 없습니다.");
                return "board/modify";
            }
        }
        // 게시글 유저가 삭제되지 않은 경우
        else
        {
            // case 1,2
            if(principal.getName().equals("admin") || board.getUsers().getUsername().equals(principal.getName()))
            {
                BoardCreateForm boardCreateForm = new BoardCreateForm();
                boardCreateForm.setId(id);
                boardCreateForm.setTitle(board.getTitle());
                boardCreateForm.setContent(board.getContent());
                boardCreateForm.setNickname(userResponseDTO.getNickname());
                boardCreateForm.setFix(board.isFix());
                model.addAttribute("boardCreateForm",boardCreateForm);
                model.addAttribute("Image",board.getBoardImages());
                return "board/modify";
            }
            // case 5
            else {
                alertAndClose(httpServletResponse,"수정 권한이 없습니다.");
                return "board/modify";
            }
        }
    }

    /*
     게시글 수정(U)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String BoardModify(@PathVariable Long id,@Valid BoardCreateForm boardCreateForm,
                              BindingResult bindingResult,Principal principal
                              ,@ModelAttribute BoardImageUploadDTO boardImageUploadDTO,HttpServletResponse httpServletResponse)
    {
        // 유효성 검사를 실패했을 경우
        if(bindingResult.hasErrors())
        {
            return "board/modify";
        }

        BoardResponseDTO boardResponseDto = this.boardService.detail(id); // 수정할 게시글 데이터 가져오기

        // GetMapping에서 case를 전부 사용해서 여기서는 따로 검증하지 않음 필요시 변경 예정
        boardService.Modify(id,boardCreateForm.getTitle(),boardCreateForm.getContent(),boardCreateForm.isFix()
                ,boardImageUploadDTO);

        return "redirect:/board/detail/"+id;

    }


    /*
      게시글 수정 중 파일삭제
     */
    @GetMapping("/Image/delete/{imageId}/{boardId}")
    @PreAuthorize("isAuthenticated()")
    public String ImageDelete(@PathVariable Long imageId,@PathVariable Long boardId)
    {
        boardService.ImageDelete(boardId,imageId);
        return "redirect:/board/modify/" + boardId;
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String BoardDelete(@PathVariable Long id,Principal principal,HttpServletResponse httpServletResponse)
    {
        BoardResponseDTO board = boardService.detail(id);

        // case 1. 관리자 => 삭제 가능
        // case 2. 게시글 작성유저 => 삭제 가능
        // case 3. 유저가 탈퇴한 게시글 관리자 => 삭제 가능
        // case 4. 유저가 탈퇴했고 관리자도 아님  => 삭제 불가
        // case 5. 게시글 작성자가 아니고 관리자도 아님 => 삭제 불가능

        // 게시글 유저가 삭제된 경우
        if(board.getUsers() == null)
        {
            // case 3
            if(principal.getName().equals("admin"))
            {
                boardService.delete(id,LocalDateTime.now());
                return "redirect:/board/list/qna";
            }
            // case 4
            else {
                alertAndClose(httpServletResponse,"삭제 권한이 없습니다.");
                return "redirect:/board/detail"+id;
            }
        }
        // 게시글 유저가 삭제되지 않은 경우
        else
        {
            // case 1,2
            if(principal.getName().equals("admin") || board.getUsers().getUsername().equals(principal.getName()))
            {
                boardService.delete(id,LocalDateTime.now());
                return "redirect:/board/list/qna";
            }
            // case 5
            else {
                alertAndClose(httpServletResponse,"삭제 권한이 없습니다.");
                return "redirect:/board/detail"+id;
            }
        }

    }

    // 추천기능
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/vote/{id}")
    public String BoardVote(Principal principal, @PathVariable("id") Long id) {

        BoardResponseDTO boardResponseDto = this.boardService.detail(id);
        UserResponseDTO users = this.userService.getUserDTO(principal.getName());

        boardService.vote(boardResponseDto, users);
        return String.format("redirect:/board/detail/%s", id);
    }

    // 비추천기능
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/notvote/{id}")
    public String BoardNotVote(Principal principal, @PathVariable("id") Long id)
    {
        BoardResponseDTO boardResponseDto = this.boardService.detail(id);
        UserResponseDTO users = this.userService.getUserDTO(principal.getName());

        boardService.notvote(boardResponseDto,users);
        return String.format("redirect:/board/detail/%s", id);
    }


    // 내 게시글 목록보기
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list/ByBoard/{userId}/{CategoryName}")
    public String UserListBoard(@PathVariable Long userId,@PathVariable String CategoryName,
                                Model model,Principal principal,
                                @RequestParam(defaultValue = "0") int page)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(userId);


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

        // 유저정보가 접속중인 유저거나 , 관리자인 경우 통과
        if(userResponseDTO.getUsername().equals(principal.getName()) || principal.getName().equals("admin"))
        {
            // 나머지는 전부 통용됨

            model.addAttribute("boardName",category);

            Page<BoardResponseDTO> paging = boardService.getPersonalBoardList(page, userResponseDTO.getUsername(),category);
            model.addAttribute("user", userResponseDTO);
            model.addAttribute("paging", paging);
            // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
            model.addAttribute("type", "총 "+Categorys+" 개수");
            return "board/personal_list";
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
        }

    }


    //알림창 띄운 후 뒤로 이동
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
