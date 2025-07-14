package hello.project.BoardProject.Service;

import hello.project.BoardProject.Entity.Board.BoardImage;
import hello.project.BoardProject.Repository.Board.BoardImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HiddenBoardImageService {

    private final BoardImageRepository boardImageRepository;
    public List<BoardImage> ImageList(Long delete_board_Id) {

        List<BoardImage> boardImageList = boardImageRepository.findByDelete_Board_Id(delete_board_Id);

        return boardImageList;
    }
}
