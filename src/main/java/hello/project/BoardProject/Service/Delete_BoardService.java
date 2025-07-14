package hello.project.BoardProject.Service;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardImage;
import hello.project.BoardProject.Entity.Board.BoardPageNumber;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Repository.Board.BoardImageRepository;
import hello.project.BoardProject.Repository.Board.BoardPageRepository;
import hello.project.BoardProject.Repository.Board.DeleteBoardRepository;
import hello.project.BoardProject.Repository.Comment.HiddenCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class Delete_BoardService {

    private final DeleteBoardRepository deleteBoardRepository;
    private final BoardPageRepository boardPageRepository;
    private final HiddenCommentRepository hiddenCommentRepository;
    private final BoardImageRepository boardImageRepository;

    public BoardResponseDTO detail(Long id)  {

        Optional<Board> board = deleteBoardRepository.findById(id);
        if(board.isEmpty())
        {
            throw new DataNotFoundException("게시글이 없습니다.");
        }
        else {
            BoardResponseDTO boardResponseDTO = new BoardResponseDTO(board.get());
            return boardResponseDTO;
        }
    }

    public Board getBoard(Long id)
    {
        Optional<Board> board = deleteBoardRepository.findById(id);
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
        Optional<Board> preBoard = deleteBoardRepository.
                findPreBoardByCreateDate(boardResponseDTO.getCreateDate());

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
        Optional<Board> nextBoard =
                deleteBoardRepository.findNextBoardByCreateDate(boardResponseDTO.getCreateDate());

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
        Optional<Board> board = deleteBoardRepository.findById(pre);
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



    public Page<BoardResponseDTO> getPage(int page, String sort, int categoryName,String search ,String kw)
    {
        Pageable pageable;

        pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC,sort,"create_date")); //페이지 번호, 개수

        if(search.equals("title"))
        {
            log.info(" 제목으로 제대로 들어옴?");
            Page<Board> boardList = deleteBoardRepository.findAllByTitleByKeywordAndType(kw,categoryName,pageable);
            Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
            return boardResponseDtoList;
        }
        else if(search.equals("content")){
            log.info(" 내용으로 제대로 들어옴?");

            Page<Board> boardList = deleteBoardRepository.findAllByContentByKeywordAndType(kw,categoryName,pageable);
            Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
            return boardResponseDtoList;
        }
        else if(search.equals("nickname")){

            log.info(" 닉네임으로 제대로 들어옴?");

            Page<Board> boardList = deleteBoardRepository.findAllByNicknameByKeywordAndType(kw,categoryName,pageable);
            Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
            return boardResponseDtoList;
        }
        else {
            log.info(" 제목/내용 으로 제대로 들어옴?");

            Page<Board> boardList = deleteBoardRepository.findAllTitleOrContentByKeywordAndType(kw, categoryName, pageable);
            Page<BoardResponseDTO> boardResponseDtoList = boardList.map(b->new BoardResponseDTO(b));
            return boardResponseDtoList;
        }

    }

    // 게시글 클리너
    public List<BoardResponseDTO> getList()
    {
        List<Board> boardList = deleteBoardRepository.findAll();
        List<BoardResponseDTO> boardResponseDTOList = boardList.stream().map(b -> new BoardResponseDTO(b)).
                collect(Collectors.toList());


        return boardResponseDTOList;
    }




    /*
     게시글 Hard 삭제
     */
    public void Board_HardDelete(Long id)
    {
        // 게시글이 없으면 오류를 발생시켜야 하므로 getBoard 선언
        Board board = getBoard(id);

        // 게시글 이미지 완전사게
        if(!boardImageRepository.findByDelete_Board_Id(id).isEmpty())
        {
            for(BoardImage boardImage : boardImageRepository.findByDelete_Board_Id(id))
            {
                File file = new File(boardImage.getUrl());
                boardImageRepository.delete(boardImage);
                file.delete();
            }
        }

        // 게시글 댓글 완전삭제
        if(!hiddenCommentRepository.findAllByBoard(id).isEmpty())
        {
            for(Comment comment : hiddenCommentRepository.findAllByBoard(id))
            {
                if(!comment.getChildren().isEmpty())
                {
                    for(Comment commentChildren : comment.getChildren())
                    {
                        hiddenCommentRepository.delete(commentChildren);
                    }
                }
                hiddenCommentRepository.delete(comment);
            }
        }

        deleteBoardRepository.deleteById(id);
    }



}
