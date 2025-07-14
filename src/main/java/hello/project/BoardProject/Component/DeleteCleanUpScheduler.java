package hello.project.BoardProject.Component;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import hello.project.BoardProject.Repository.Board.DeleteBoardRepository;
import hello.project.BoardProject.Repository.Users.OAuth2AccesTokenDataRepository;
import hello.project.BoardProject.Service.Delete_BoardService;
import hello.project.BoardProject.Service.Delete_UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RequiredArgsConstructor
@Component
@Slf4j
public class DeleteCleanUpScheduler {

    private final Delete_BoardService delete_boardService;
    private final Delete_UserService deleteUserService;
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void accesTokenDelete()
    {
        List<OAuth2AccesTokenData> oAuth2AccesTokenDataList =oAuth2AccesTokenDataRepository.findAll();

        for(OAuth2AccesTokenData oAuth2AccesTokenData : oAuth2AccesTokenDataList)
        {
            // 현재 시간이 데이터 생성 10분보다 미래여야함
            // ex) 데이터 생성이 2025-06-25(1010) 인경우
            // 현재시간이 2022-06-25(1021) 인 경우
            // 이러면 데이터 삭제 조건이 충족
            if(LocalDateTime.now().isAfter(oAuth2AccesTokenData.getCreateDate().plusMinutes(10)) ||
                    LocalDateTime.now().isEqual(oAuth2AccesTokenData.getCreateDate().plusMinutes(10)))
            {
                // 토큰값 삭제
                oAuth2AccesTokenDataRepository.delete(oAuth2AccesTokenData);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void BoardDelete() {
        log.info("소프트 삭제 후 2개월 지난 게시글 삭제 스케줄러 동작");
        List<BoardResponseDTO> boardResponseDTOPage = delete_boardService.getList();

        for (BoardResponseDTO boardResponseDTO : boardResponseDTOPage) {
            if(boardResponseDTO.getDelete_createDate() != null)
            {
                LocalDateTime plusOneHour = boardResponseDTO.getDelete_createDate().plusMonths(1);

                if(plusOneHour.isBefore(LocalDateTime.now()))
                {
                    delete_boardService.Board_HardDelete(boardResponseDTO.getId());
                }
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void UserDelete()
    {
        List<UserResponseDTO> userResponseDTOList = deleteUserService.getList();

        for(UserResponseDTO userResponseDTO : userResponseDTOList)
        {
            if(userResponseDTO.getUser_delete_createDate() != null)
            {
                LocalDateTime plusOneHour = userResponseDTO.getUser_delete_createDate().plusMonths(1);

                if(plusOneHour.isBefore(LocalDateTime.now()))
                {
                    deleteUserService.Hard_Delete(userResponseDTO.getId());
                }
            }
        }
    }

}
