package hello.project.BoardProject.Service;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Repository.Comment.HiddenCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HiddenCommentService {

    private final HiddenCommentRepository hiddenCommentRepository;

    // 삭제된 게시글의 댓글 페이징 처리
    public Page<CommentResponseDTO> findAll(int page, Long board_Id) {

        Pageable pageable;
        pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC,  "create_Date"));

        Page<Comment> commentList = hiddenCommentRepository.findAllByBoard(pageable, board_Id);

        Page<CommentResponseDTO> commentResponseDTOS = commentList.map(b -> new CommentResponseDTO(b));

        return commentResponseDTOS;
    }




}
