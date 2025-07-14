package hello.project.BoardProject.Service;

import hello.project.BoardProject.DTO.Board.BoardImageUploadDTO;
import hello.project.BoardProject.DTO.Board.Response.Board_Views_ResponseDTO;
import hello.project.BoardProject.DTO.Board.Request.BoardRequestDTO;
import hello.project.BoardProject.DTO.Board.Response.BoardNotVotorResponseDTO;
import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Board.Response.BoardVotorResponseDTO;
import hello.project.BoardProject.DTO.ChartData;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.*;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.LoginLog;
import hello.project.BoardProject.Entity.Users.Message;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Repository.Board.*;
import hello.project.BoardProject.Repository.Comment.CommentRepository;
import hello.project.BoardProject.Repository.Users.DeleteUserRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardPageRepository boardPageRepository;
    private final BoardVoterRepository boardVoterRepository;
    private final BoardNotVoterRepository boardNotVoterRepository;
    private final BoardImageRepository boardImageRepository;
    private final DeleteBoardRepository deleteBoardRepository;
    private final BoardReadRepository boardReadRepository;
    private final Board_ViewsRepository boardViewsRepository;
    private final CommentRepository commentRepository;
    private final DeleteUserRepository deleteUserRepository;


    @Value("${file.boardImagePath}")
    private String uploadFolder;


    /*
      게시글 작성(C)
     */
    public void save(String title, String content,boolean fix, String username,
                     int category, List<MultipartFile> files)
    {
        BoardRequestDTO boardRequestDTO = new BoardRequestDTO();
        Users users = userRepository.findByusername(username).orElseThrow(()-> new IllegalArgumentException("해당 유저 없는데요?"));

        // 게시글 저장
        boardRequestDTO.setTitle(title);
        boardRequestDTO.setContent(content);
        boardRequestDTO.setUsers(users);
        boardRequestDTO.setCateogory(category);
        boardRequestDTO.setFix(fix);
        boardRequestDTO.setCreateDate(LocalDateTime.now());
        Board board = boardRequestDTO.toEntity();
        boardRepository.save(board);

        // 게시글 이미지 저장
        if (files != null && !files.isEmpty()) {

            for (MultipartFile file : files) { // 다중 파일만큼 반복
                if(file.getSize() > 0 )
                {
                    UUID uuid = UUID.randomUUID();  //  범용 고유 식별자(UUID)를 생성하는 코드
                    String imageFileName = uuid + "_" + file.getOriginalFilename(); //  UUID와 파일의 원본 파일명을 연결하여 이미지 파일명을 생성하는 코드

                    File destinationFile = new File(uploadFolder + imageFileName); // 파일의 경로, 파일 이름

                    try {
                        //   MultipartFile에서 제공하는 메서드 중에 하나로,
                        // 업로드된 파일을 지정된 경로에 저장하는 역할을 함
                        file.transferTo(destinationFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    BoardImage image = BoardImage.builder()
                            .url("/boardImages/" + imageFileName)
                            .board(board)
                            .build();

                    boardImageRepository.save(image);
                }
            }
        }
    }

    /*
      상세 게시글 조회(R)
     */
    public BoardResponseDTO detail(Long id)  {

        Optional<Board> board = boardRepository.findById(id);

        if(board.isEmpty())
        {
            throw new DataNotFoundException("게시글이 없습니다.");
        }
        else {

            BoardResponseDTO boardResponseDTO = new BoardResponseDTO(board.get());
            return boardResponseDTO;
        }
    }

    /*
      엔티티 데이터가 필요할때 사용
     */
    public Board getBoard(Long id)  {

        Optional<Board> board = boardRepository.findById(id);
        if(board.isEmpty())
        {
            throw new DataNotFoundException("게시글이 없습니다.");
        }
        else {
            return board.get();
        }
    }


    // 이전 다음글 로직
    public Long getPrePageNumber(BoardResponseDTO boardResponseDTO)
    {
        Optional<Board> preBoard = boardRepository.findPreBoardByCreateDate(boardResponseDTO.getCreateDate());

        if(preBoard.isEmpty())
        {
            return null;
        }
        else {
            return preBoard.get().getId();
        }
    }

    public Long getNextPageNumber(BoardResponseDTO boardResponseDTO)
    {
        Optional<Board> nextBoard = boardRepository.findNextBoardByCreateDate(boardResponseDTO.getCreateDate());

        if(nextBoard.isEmpty())
        {
            return null;
        }
        else {
            return nextBoard.get().getId();
        }
    }

    /*
     이전 페이지가 없다고 오류가 나면 안되니 없을 경우 null
     */
    public BoardResponseDTO getPreBoardDTO(Long pre)
    {
        Optional<Board> board = this.boardRepository.findById(pre);
        if(board.isPresent())
        {
            BoardResponseDTO boardGetResponseDto = new BoardResponseDTO(board.get());
            return boardGetResponseDto;
        }
        else
        {
            return null;
        }
    }

    /*
      이전 게시글 로직 리턴
     */
    public BoardResponseDTO getPrePage(BoardResponseDTO boardResponseDTO)
    {

        Long pre = getPrePageNumber(boardResponseDTO); // 이전 게시글 찾기

        if(pre == null)
        {
            return null;
        }

        while (pre != null)
        {
            BoardResponseDTO preDTO = getPreBoardDTO(pre); // 이전 게시글 DTO

            // 이전 게시글이 있는 경우
            if(preDTO != null)
            {
                // 이전 게시글에서 카테고리가 서로 같은경우(다른 카테고리면 반복문 계속)
                if((preDTO.getCategory() == boardResponseDTO.getCategory())) // 이전 게시글의 카테고리 넘버 == 현재 게시글 카테고리 넘버 일 경우
                {
                    return preDTO;
                }
                // 카테고리가 서로 다를 경우
                else {
                    pre = getPrePageNumber(preDTO); // 이전 게시글의 또 이전 게시글 가져오기
                }
            }

        }
        return null; // 조건문에 안들어가는 경우는 당연히 NULL

    }

    /*
      다음 게시글 로직 리턴
     */
    public BoardResponseDTO getNextPage(BoardResponseDTO boardResponseDTO)
    {
        Long next = getNextPageNumber(boardResponseDTO); // 이전 게시글 찾기

        if(next == null)
        {
            return null;
        }

        while (next != null)
        {
            BoardResponseDTO nextDTO = getPreBoardDTO(next); // 이전 게시글 DTO

            // 다음 게시글이 있는 경우
            if(nextDTO != null)
            {
                // 이전 게시글에서 카테고리가 서로 같은경우(다른 카테고리면 반복문 계속)
                if((nextDTO.getCategory() == boardResponseDTO.getCategory())) // 이전 게시글의 카테고리 넘버 == 현재 게시글 카테고리 넘버 일 경우
                {
                    return nextDTO;
                }
                // 카테고리가 서로 다를 경우
                else {
                    next = getNextPageNumber(nextDTO); // 이전 게시글의 또 이전 게시글 가져오기
                }
            }
        }

        return null; // 조건문에 안들어가는 경우는 당연히 NULL
    }



    /*
     쿠키기반 조회수 로직
     */
    @Transactional
    public void updateViews(Long id, HttpServletRequest request,
                           HttpServletResponse response,BoardResponseDTO boardResponseDTO)
    {
        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies(); // request 객체의 쿠키들을 가져와 리스트애 담음

        if (cookies != null) {
            for (Cookie cookie : cookies) { // 쿠키 리스트를 찾아본다
                if (cookie.getName().equals("boardViews")) { // 쿠키 리스트 이름중에 "boardViews" 가 있을 경우
                    oldCookie = cookie; // boardViews 의 이름을 담음
                }
            }
        }
        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("["+ id.toString() +"]")) { // id 값이 없는경우(조회를 안한 경우)
                boardRepository.updateView(id); // 조회수 증가
                ViewsData_ReStore(id,boardResponseDTO.getCategory()); // 조회수 엔티티 저장
                oldCookie.setValue(oldCookie.getValue() + "_[" + id + "]"); // id 값 추가
                oldCookie.setPath("/");
                oldCookie.setMaxAge(60 * 60 * 24); 							// 쿠키 시간
                response.addCookie(oldCookie); // 쿠키 유지시간 경로를 추가하여 response에 oldCookie 전달
            }
        } else { // oldCookie == null
            boardRepository.updateView(id);
            ViewsData_ReStore(id,boardResponseDTO.getCategory());
            Cookie newCookie = new Cookie("boardViews", "[" + id + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24); 								// 쿠키 시간
            response.addCookie(newCookie);
        }

    }


    /*
     고정 게시글 리스트
     */
    public List<BoardResponseDTO> FixList(int categoryName)
    {
        List<Board> boardList = boardRepository.findByCategoryAndFixOrderByCreateDateDesc(categoryName, true);

        // 고정 게시글이 카테고리 내에서 3개가 넘어가면
        while(boardList.size() > 3)
        {
            Board board = boardRepository.findFirstByCategoryAndFixOrderByCreateDateAsc(categoryName,true); // 오래된 고정게시글 삭제
            Long boardId= board.getId();
            boardList.remove(board); // 리스트 목록에서 고정 게시글 삭제
            boardRepository.delete(board);
            deleteBoardRepository.deleteById(boardId); // 첫번째 고정게시글 완전삭제
        }

        List<BoardResponseDTO> boardResponseDTOList = boardList.stream().map(b -> new BoardResponseDTO(b)).collect(Collectors.toList());

        return boardResponseDTOList;
    }


    public Page<BoardResponseDTO> getPage(int page, String sort,
                                          int category, String search, String kw) {

        Pageable pageable;

        if(sort.equals("views"))
        {
            pageable = PageRequest.of(page, 10, Sort.Direction.DESC,sort,"createDate"); //페이지 번호, 개수

            if(search.equals("title"))
            {
                Page<Board> boardList = boardRepository.findAllByTitleByKeywordAndType(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else if(search.equals("content")){
                Page<Board> boardList = boardRepository.findAllByContentByKeywordAndType(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else if(search.equals("nickname")){
                Page<Board> boardList = boardRepository.findAllByNicknameByKeywordAndType(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else {
                Page<Board> boardList = boardRepository.findAllTitleOrContentByKeywordAndType(kw, category, pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
        }
        else if(sort.equals("voters"))
        {
            pageable = PageRequest.of(page, 10, Sort.Direction.DESC,"createDate"); //페이지 번호, 개수

            if(search.equals("title"))
            {
                Page<Board> boardList = boardRepository.findAllByTitleByKeywordAndTypeAndVoters(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else if(search.equals("content")){
                Page<Board> boardList = boardRepository.findAllByContentByKeywordAndTypeAndVoters(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else if(search.equals("nickname")){
                Page<Board> boardList = boardRepository.findAllByNicknameByKeywordAndTypeAndVoters(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else {
                Page<Board> boardList = boardRepository.findAllTitleOrContentByKeywordAndTypeAndVoters(kw, category, pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
        }
        else {

            pageable = PageRequest.of(page, 10, Sort.Direction.DESC,sort); //페이지 번호, 개수

            if(search.equals("title"))
            {
                Page<Board> boardList = boardRepository.findAllByTitleByKeywordAndType(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else if(search.equals("content")){
                Page<Board> boardList = boardRepository.findAllByContentByKeywordAndType(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else if(search.equals("nickname")){
                Page<Board> boardList = boardRepository.findAllByNicknameByKeywordAndType(kw,category,pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
            else {
                Page<Board> boardList = boardRepository.findAllTitleOrContentByKeywordAndType(kw, category, pageable);
                Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
                return boardResponseDtoList;
            }
        }

    }


    /*
        게시글 수정(U)
     */
    public void Modify(Long id,String title,String content,boolean fix,BoardImageUploadDTO boardImageUploadDTO)
    {
        Board board = getBoard(id); // 수정할 게시글 엔티티 가져오기
        board.BoardUpdate(title,content,fix);
        boardRepository.save(board);

        // 이 부분 추가
        if (boardImageUploadDTO.getFiles() != null && !boardImageUploadDTO.getFiles().isEmpty()) {
            for (MultipartFile file : boardImageUploadDTO.getFiles()) {
                if(file.getSize() != 0)
                {
                    UUID uuid = UUID.randomUUID();
                    String imageFileName = uuid + "_" + file.getOriginalFilename();

                    File destinationFile = new File(uploadFolder + imageFileName);

                    try {
                        file.transferTo(destinationFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    BoardImage image = BoardImage.builder()
                            .url("/boardImages/" + imageFileName)
                            .board(board)
                            .build();

                    boardImageRepository.save(image);
                }
            }
        }

    }

    /*
      게시글 SOFT 삭제
     */
    public void delete(Long id,LocalDateTime deleteTime)
    {
        Board board = boardRepository.findById(id).orElseThrow();
        board.DeleteCreateDate(deleteTime);
        boardRepository.save(board);

        List<Comment> commentList = commentRepository.findByBoard(board);
        List<BoardImage> boardImages = boardImageRepository.findByBoard(board);


        // 댓글이 있는 경우
        if(!commentList.isEmpty())
        {
            List<Comment> comments = commentList;
            // 댓글 반복문 돌리기
            for(Comment comment : comments)
            {
                comment.Board_Soft_Delete(board.getId());
                commentRepository.save(comment);
            }
        }

        // 게시글 이미지가 있는 경우
        if(!boardImages.isEmpty())
        {
            List<BoardImage> boardImageList = boardImages;

            for(BoardImage boardImage : boardImageList)
            {
                boardImage.Board_Soft_Delete(board.getId());

                boardImageRepository.save(boardImage);
            }
        }

        boardRepository.delete(board);
    }


    // 추천을 누르는 경우 해당 메소드 동작
    public void vote(BoardResponseDTO boardResponseDto, UserResponseDTO userResponseDTO)
    {
        Board board = getBoard(boardResponseDto.getId());
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저는 존재하지 않습니다"));


        // 유저가 게시글 내에서 추천을 했는지 찾기
        BoardVoter boardVoter = boardVoterRepository.findByBoardAndVoter(board,users);

        // 유저가 게시글 내에서 비추천을 하였는지 찾기
        BoardNotVoter boardNotVoter = boardNotVoterRepository.findByBoardAndNotVoter(board,users);

        // 추천을 한 경우
        if(boardVoter != null)
        {
            // 추천을 한 유저가 비추천을 안한 경우(사실 이게 일반적이긴함)
            if(boardNotVoter == null)
            {
                // 추천 클릭 이벤트므로 추천취소로 해석
                board.VoteMinus(boardVoter);
                this.boardRepository.save(board); // orphanRemoval = true 하였으므로 고아객체가 되서 추천 엔티티가 자동삭제됨
            }
        }

        // 추천을 안한 경우
        else
        {
            // 비추천도 안한 경우 => 추천을 누르면 추천이 + 됨
            if(boardNotVoter ==null)
            {
                BoardVoter newBoardVoter = BoardVoter.builder().
                        voter(users)
                                .board(board).createDate(LocalDateTime.now()).
                        build();

                boardVoterRepository.save(newBoardVoter);
            }
        }
    }

    // 비추천을 누를경우 이벤트
    public void notvote(BoardResponseDTO boardResponseDto,UserResponseDTO userResponseDTO)
    {
        Board board = getBoard(boardResponseDto.getId());
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저는 존재하지 않습니다"));

        BoardVoter boardVoter = boardVoterRepository.findByBoardAndVoter(board,users);
        BoardNotVoter boardNotVoter = boardNotVoterRepository.findByBoardAndNotVoter(board,users);

        // 비추천이 클릭되어 있는 경우
        if(boardNotVoter != null)
        {
            // 추천을 안한 경우 => 비추천을 취소함
            if(boardVoter == null)
            {
                board.NotVoterMinus(boardNotVoter);
                this.boardRepository.save(board);
            }
        }

        // 비추천을 안 누른 경우
        else
        {
            // 추천을 안누름 => 비추천을 함
            if(boardVoter == null)
            {
                BoardNotVoter newBoardNotVoter = BoardNotVoter.builder().
                        notVoter(users)
                        .board(board).createDate(LocalDateTime.now()).
                        build();

                boardNotVoterRepository.save(newBoardNotVoter);
            }
        }
    }

    public List<BoardVotorResponseDTO> getBoardVoterList(BoardResponseDTO boardResponseDTO) {

        Board board = getBoard(boardResponseDTO.getId());
        List<BoardVoter> boardVoterList = boardVoterRepository.findByBoard(board);

        List<BoardVotorResponseDTO> boardVotorResponseDTOS = boardVoterList.
                stream().map(b -> new BoardVotorResponseDTO(b)).collect(Collectors.toList());

        return boardVotorResponseDTOS;
    }

    public List<BoardNotVotorResponseDTO> getBoardNotVoterList(BoardResponseDTO boardResponseDTO) {
        Board board = getBoard(boardResponseDTO.getId());

        List<BoardNotVoter> boardNotVoterList = boardNotVoterRepository.findByBoard(board);
        List<BoardNotVotorResponseDTO> boardNotVotorResponseDTOS = boardNotVoterList.
                stream().map(b -> new BoardNotVotorResponseDTO(b)).collect(Collectors.toList());

        return boardNotVotorResponseDTOS;
    }


    public boolean voteUsers(BoardResponseDTO boardResponseDto,UserResponseDTO userResponseDTO)
    {
        Board board = getBoard(boardResponseDto.getId());
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저는 존재하지 않습니다"));

        // 주어진 스트림에서 적어도 한 요소와 일치하는지(true인지) 확인할 때 anyMatch 메서드를 이용

        List<BoardVoter> boardVoterList = boardVoterRepository.findByBoard(board);

        if(!boardVoterList.isEmpty())
        {
            boardVoterList.removeIf(boardVoter -> boardVoter.getVoter() == null);
        }

        return boardVoterList.stream().anyMatch(v -> v.getVoter().equals(users));
    }

    public boolean notvoteUsers(BoardResponseDTO boardResponseDto,UserResponseDTO userResponseDTO)
    {
        Board board = getBoard(boardResponseDto.getId());
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저는 존재하지 않습니다"));

        List<BoardNotVoter> boardNotVoterList = boardNotVoterRepository.findByBoard(board);

        if(!boardNotVoterList.isEmpty())
        {
            boardNotVoterList.removeIf(boardNotVoter -> boardNotVoter.getNotVoter() == null);
        }


       return boardNotVoterList.stream().anyMatch(v -> v.getNotVoter().equals(users));

    }


    /*
      메인 게시글 탑 10개 출력
     */
    public List<BoardResponseDTO> getMainListAll()
    {
        List<Board> boardList = boardRepository.findTop10ByOrderByCreateDateDesc();
        List<BoardResponseDTO> boardResponseDTOList = boardList.stream().map(b -> new BoardResponseDTO(b)).collect(Collectors.toList());
        return boardResponseDTOList;
    }

    /*
     메인 리스트 카테고리별 탑10 출력
     */
    public List<BoardResponseDTO> getMainList(int boardName)
    {
        List<Board> boardList = boardRepository.findTop10ByCategoryOrderByCreateDateDesc(boardName);
               List<BoardResponseDTO> boardResponseDtoList = boardList.stream().map(b->
                               new BoardResponseDTO(b)).
                     collect(Collectors.toList());
        return boardResponseDtoList;
    }


    // 메인 리스트 VIEWS 최근 3개
    public List<BoardResponseDTO> getMainViewsList()
    {
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        List<Board> boardList = boardRepository.findTop3ByOrderByViewsDesc(sort);
        List<BoardResponseDTO> boardResponseDtoList = boardList.stream().map(b->
                        new BoardResponseDTO(b)).
                collect(Collectors.toList());
        return boardResponseDtoList;
    }


    // 메인 리스트 카테고리별 VIEWS 최근 3개
    public List<BoardResponseDTO> getMainViewsList(int category)
    {
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        List<Board> boardList = boardRepository.findTop3ByCategoryOrderByViewsDesc(category,sort);
        List<BoardResponseDTO> boardResponseDtoList = boardList.stream().map(b->
                        new BoardResponseDTO(b)).
                collect(Collectors.toList());
        return boardResponseDtoList;
    }

    // 메인페이지 추천 리스트
    public List<BoardResponseDTO> getMainVoterList()
    {
        List<Board> boardList = boardRepository.findByOrderByVotersCountMinusNotVotersCountDesc();

        List<BoardResponseDTO> boardResponseDtoList = boardList.stream().map(b->
                        new BoardResponseDTO(b)).
                collect(Collectors.toList());
        return boardResponseDtoList;
    }


    // 카테고리별 추천순
    public List<BoardResponseDTO> getMainVoterList(int category)
    {
        List<Board> boardList = boardRepository.findByCategoryByOrderByVotersCountMinusNotVotersCountDesc(category);
        List<BoardResponseDTO> boardResponseDtoList = boardList.stream().map(b->
                        new BoardResponseDTO(b)).
                collect(Collectors.toList());
        return boardResponseDtoList;

    }


    // 소프트 삭제 안된 유저기준 게시글 개수
    public Long getBoardCount(UserResponseDTO users) {
        Users user = userRepository.findByusername(users.getUsername()).orElseThrow(null);
        // 삭제된 게시글은 @Where로 조회가 안되므로 삭제된 게시글 개수는 제외됨
        return boardRepository.countByUsers(user);
    }

    // 소프트 삭제 된 유저기준 게시글 개수
    public Long getDeleteUserBoardCount(UserResponseDTO userResponseDTO)
    {
        Users users = deleteUserRepository.findByusername(userResponseDTO.getUsername()).get();

        return boardRepository.countByDelete_User_Id(users.getId());
    }


    // 최근 5개 게시글 가져오기
    public List<BoardResponseDTO> getBoardTop5LatestByUser(UserResponseDTO users) {
        Users user = userRepository.findByusername(users.getUsername()).orElseThrow(null);

        // 해당 유저가 작성한 게시글중 최근 5개 게시글 정보 가져오기
        List<Board> boardList =  boardRepository.findTop5ByUsersOrderByCreateDateDesc(user);
        List<BoardResponseDTO> boardResponseDtoList = boardList.stream().map(b-> new BoardResponseDTO(b)).
                     collect(Collectors.toList());
        return boardResponseDtoList;
    }

    // 소프트 삭제된 유저의  최근 5개 게시글 가져오기
    public List<BoardResponseDTO> getDeleteUserBoardTop5LatestByUser(UserResponseDTO userResponseDTO) {

        Users users = deleteUserRepository.findByusername(userResponseDTO.getUsername()).get();

        List<Board> boardList = boardRepository.findTop5ByDelete_User_IdOrderByCreateDateDesc(users.getId());
        List<BoardResponseDTO> boardResponseDtoList = boardList.stream().map(b-> new BoardResponseDTO(b)).
                collect(Collectors.toList());
        return boardResponseDtoList;
    }

    /*
        카테고리별 유저가 작성한 게시글 페이징
     */
    public Page<BoardResponseDTO> getPersonalBoardList(int page,String username,int category)
    {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); //페이지 번호, 개수
        Users users = userRepository.findByusername(username).orElseThrow(()->new IllegalArgumentException("해당 유저없음"));
        Page<Board> board = boardRepository.findByCategoryAndUsers(category,users, pageable);

        Page<BoardResponseDTO> boardResponseDtos = board.map(b ->new BoardResponseDTO(b));
        return boardResponseDtos;
    }

    public void ImageDelete(Long id, Long ImageId)
    {
        //첨부파일 조회
        BoardImage boardImage = boardImageRepository.findById(ImageId).orElseThrow(()->new RuntimeException());

        //첨부파일 삭제
        File file = new File(boardImage.getUrl());
        boardImageRepository.deleteById(boardImage.getId());
        file.delete();
    }


    /*
      게시글 읽음처리
     */
    public void markBoardAsRead(Long id, String username) {

        Users users = userRepository.findByusername(username).orElseThrow();
        Board board = getBoard(id);

        Optional<BoardRead> ReadCheck = boardReadRepository.findByUsersAndBoard(users,board);
        BoardRead boardRead;

        if(ReadCheck.isEmpty())
        {
            boardRead = BoardRead.builder().
                    board(board)
                            .users(users).
                    build();
            boardReadRepository.save(boardRead);
        }
    }


    public ChartData BoardChart(int category) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        Map<String, Integer> BoardCreateCounts = new LinkedHashMap<>();

        List<Board> boardList = boardRepository.findByCategoryAndCreateDateBetweenOrderByCreateDateAsc(category,oneMonthAgo,now);

        Collections.sort(boardList, Comparator.comparing(Board::getCreateDate));


        for(Board board : boardList)
        {
            String date = board.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BoardCreateCounts.put(date, BoardCreateCounts.getOrDefault(date, 0) + 1);
        }

        for (String date : BoardCreateCounts.keySet()) {
            labels.add(date);
            values.add(BoardCreateCounts.get(date));
        }

        ChartData chartData = new ChartData(labels,values);

        return chartData;

    }

    public List<BoardResponseDTO> BoardCategoryList(int category)
    {
        List<Board> boardList = boardRepository.findByCategory(category);

        List<BoardResponseDTO> boardResponseDTOList = boardList.stream().
                map(b -> new BoardResponseDTO(b)).collect(Collectors.toList());

        return boardResponseDTOList;
    }

    public void ViewsData_ReStore(Long id,int category)
    {
        Board_Views boardViews = Board_Views.builder().
                boardId(id).category(category).viewsTime(LocalDateTime.now()).
                build();

        boardViewsRepository.save(boardViews);
    }


    public List<Long> ReadBoardIdList(String username) {

        Users users = userRepository.findByusername(username).orElseThrow(() ->
                new IllegalArgumentException("해당 유저는 존재하지 않습니다"));
        if(boardReadRepository.findByUsers(users).size()>0)
        {
            List<Board> ReadBoard = boardReadRepository.findByUsers(users).stream()
                    .map(BoardRead :: getBoard)
                    .collect(Collectors.toList());

            List<Long> ReadBoardId = ReadBoard.stream().map(Board :: getId).collect(Collectors.toList());
            return ReadBoardId;
        }

        return Collections.singletonList(0L);
    }

    public ChartData BoardViewChart(int category)
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        Map<String, Integer> BoardCounts = new LinkedHashMap<>();

        List<Board_Views> boardViewsList = boardViewsRepository.
                findByCategoryAndViewsTimeBetweenOrderByViewsTimeAsc(category,oneMonthAgo,now);

        Collections.sort(boardViewsList, Comparator.comparing(Board_Views::getViewsTime));

        for (Board_Views boardViews : boardViewsList) {
            String date = boardViews.getViewsTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BoardCounts.put(date, BoardCounts.getOrDefault(date, 0) + 1);
        }

        for (String date : BoardCounts.keySet()) {
            labels.add(date);
            values.add(BoardCounts.get(date));
        }

        ChartData chartData = new ChartData(labels,values);

        return chartData;


    }

    public List<String> Board_Voter_Nickname_list(BoardResponseDTO board) {

        List<BoardVotorResponseDTO> boardVotorResponseDTOS = getBoardVoterList(board);
        List<String> board_voter_nickname_list = new ArrayList<>();


        for(BoardVotorResponseDTO boardVotorResponseDTO : boardVotorResponseDTOS)
        {
            // 추천유저가 탈퇴하지 않은 경우에만 닉네임 등록
            if(boardVotorResponseDTO.getVoter() != null)
            {
                board_voter_nickname_list.add(boardVotorResponseDTO.getVoter().getNickname());
            }
            else {
                board_voter_nickname_list.add("탈퇴한 유저가 추천했습니다");
            }
        }

        return board_voter_nickname_list;
    }

    public List<String> Board_Not_Voter_Nickname_list(BoardResponseDTO board) {

        List<BoardNotVotorResponseDTO> boardNotVotorResponseDTOList = getBoardNotVoterList(board);
        List<String> board_not_voter_nickname_list = new ArrayList<>();

        for(BoardNotVotorResponseDTO boardNotVotorResponseDTO : boardNotVotorResponseDTOList)
        {
            // 추천유저가 탈퇴한 경우
            if(boardNotVotorResponseDTO.getNotVoter() != null)
            {
                board_not_voter_nickname_list.add(boardNotVotorResponseDTO.getNotVoter().getNickname());
            }
            else {
                board_not_voter_nickname_list.add("탈퇴한 유저가 비추천했습니다.");
            }
        }
        return board_not_voter_nickname_list;
    }


}
