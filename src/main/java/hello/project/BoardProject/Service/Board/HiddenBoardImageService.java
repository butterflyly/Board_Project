package hello.project.BoardProject.Service.Board;

import hello.project.BoardProject.DTO.Board.Response.BoardImageResponseDTO;
import hello.project.BoardProject.Entity.Board.BoardImage;
import hello.project.BoardProject.Repository.Board.BoardImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
/*
 삭제된 게시글의 이미지 서비스 로직
 */
public class HiddenBoardImageService {

    private final BoardImageRepository boardImageRepository;
    public List<BoardImageResponseDTO> ImageList(Long delete_board_Id) {

        List<BoardImage> boardImageList = boardImageRepository.findByDelete_Board_Id(delete_board_Id);

        List<BoardImageResponseDTO> boardImageResponseDTOList =
                boardImageList.stream().map(b -> new BoardImageResponseDTO(b)).
                        collect(Collectors.toList());

        return boardImageResponseDTOList;
    }
}
