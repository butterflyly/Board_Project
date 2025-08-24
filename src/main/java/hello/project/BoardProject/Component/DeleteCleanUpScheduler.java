package hello.project.BoardProject.Component;

import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import hello.project.BoardProject.Repository.Users.OAuth2AccesTokenDataRepository;
import hello.project.BoardProject.Service.Board.Delete_BoardService;
import hello.project.BoardProject.Service.Users.Delete_UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@RequiredArgsConstructor
@Component
@Slf4j
/*
  소프트 삭제 데이터 or 특정 시간만 존재하는 데이터를 기간을 설정하여 HARD DELETE 시키는 클래스
 */
public class DeleteCleanUpScheduler {

    private final Delete_BoardService delete_boardService;
    private final Delete_UserService deleteUserService;
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;

    // 엑세스 토큰 삭제 메소드
    @Scheduled(fixedRate = 500000)
    @Transactional
    public void accesTokenDelete()
    {
        List<OAuth2AccesTokenData> oAuth2AccesTokenDataList =oAuth2AccesTokenDataRepository.findAll();

        for(OAuth2AccesTokenData oAuth2AccesTokenData : oAuth2AccesTokenDataList)
        {
            // 현재 시간이 데이터 생성 1시간보다 미래여야함
            // ex) 데이터 생성이 2025-06-25(1010) 인경우
            // 현재시간이 2022-06-25(1021) 인 경우
            // 이러면 데이터 삭제 조건이 충족
            if(oAuth2AccesTokenData.getToken() != null)
            {
                if(LocalDateTime.now().isAfter(oAuth2AccesTokenData.getAccessCreateDate().plusMinutes(40)) ||
                        LocalDateTime.now().isEqual(oAuth2AccesTokenData.getAccessCreateDate().plusMinutes(40)))
                {
                    // 액세스 토큰값 null 로 변환
                    oAuth2AccesTokenData.setToken(null);
                    oAuth2AccesTokenDataRepository.save(oAuth2AccesTokenData);
                }
            }

            if(oAuth2AccesTokenData.getRefreshToken() != null)
            {
                // 리프레시 토큰 삭제 (약 한달)
                if(LocalDateTime.now().isAfter(oAuth2AccesTokenData.getRefreshCreateDate().plusMonths(1)) ||
                        LocalDateTime.now().isEqual(oAuth2AccesTokenData.getRefreshCreateDate().plusMonths(1)))
                {
                    oAuth2AccesTokenData.setRefreshToken(null);
                    oAuth2AccesTokenDataRepository.save(oAuth2AccesTokenData);
                }
            }
        }
    }

    // 소프트 삭제 게시글 하드삭제
    @Scheduled(cron = "0 0 0 * * *") // 자정에 동작
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

    // 소프트 유저 리스트 하드삭제
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
