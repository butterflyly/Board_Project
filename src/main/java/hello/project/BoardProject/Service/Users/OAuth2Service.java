package hello.project.BoardProject.Service.Users;

import hello.project.BoardProject.DTO.Users.UserRequestDTO;
import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import hello.project.BoardProject.Entity.Users.UserRole;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Form.Users.UserRegisterForm;
import hello.project.BoardProject.OAuth2.Google.GoogleTokenResponse;
import hello.project.BoardProject.OAuth2.Naver.NaverTokenResponse;
import hello.project.BoardProject.OAuth2.Naver.UnlinkResponse;
import hello.project.BoardProject.Repository.Users.OAuth2AccesTokenDataRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.zip.DataFormatException;


@RequiredArgsConstructor
@Service
@Slf4j
public class OAuth2Service {

    private final UserRepository userRepository;

    // 리프레시 토큰
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String google_clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String google_clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String google_redirect_url;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String google_token_url;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String google_user_info_url;

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String NAVER_TOKEN_URL;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String NAVER_REDIRECT_URL;

    @Value("${spring.security.oauth2.client.provider.naver.authorization-uri}")
    private String NAVER_AUTHORIZEATION_URL;

    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String NAVER_USER_INFO_URL;

    /*
      소셜 액세스 토큰
     */
    public String accessToken(String username)
    {
        Optional<OAuth2AccesTokenData> oAuth2AccesTokenData = oAuth2AccesTokenDataRepository.findByUsername(username);

        if(!oAuth2AccesTokenData.isEmpty())
        {
            String accessToken = oAuth2AccesTokenData.get().getToken();

            return accessToken;
        }
        else {
            return null;
        }
    }


    /*
        구글 토큰 발급
     */
    public String refreshAccessToken(String username) {

        RestTemplate restTemplate = new RestTemplate();

        String tokenUrl = "https://oauth2.googleapis.com/token";

        Optional<OAuth2AccesTokenData> oAuth2AccesTokenData =
                oAuth2AccesTokenDataRepository.findByUsername(username);

        String refreshToken = oAuth2AccesTokenData.get().getRefreshToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", google_clientId);
        body.add("client_secret", google_clientSecret);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        // 토큰 요청 및 응답 처리
        GoogleTokenResponse tokenResponse = restTemplate.postForObject
                (tokenUrl, request, GoogleTokenResponse.class);

        if (tokenResponse != null && tokenResponse.getAccessToken() != null) {

            return tokenResponse.getAccessToken();
        } else {
            // 에러 처리 로직 (예: 로그 출력, 예외 처리)
            throw new RuntimeException("Failed to refresh access token: " + tokenResponse);
        }
    }


    /*
     네이버 액세스 토큰 재발급
     */
    public String reissueAccessToken(String username) {

        Optional<OAuth2AccesTokenData> oAuth2AccesTokenData =
                oAuth2AccesTokenDataRepository.findByUsername(username);

        String refreshToken = "";

        if(!oAuth2AccesTokenData.isEmpty() &&
                oAuth2AccesTokenData.get().getRefreshToken() != null && oAuth2AccesTokenData.get().getToken() == null)
        {
            refreshToken = oAuth2AccesTokenData.get().getRefreshToken();
        }
        else {
            throw new DataNotFoundException("리프레시 토큰이 만료되었습니다");
        }

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", NAVER_CLIENT_ID);
        params.add("client_secret", NAVER_CLIENT_SECRET);
        params.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<NaverTokenResponse> response = restTemplate.postForEntity(NAVER_TOKEN_URL, request, NaverTokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getAccessToken();
        } else {
            // 에러 처리 로직 (예: 로그 기록, 예외 발생)
            System.err.println("Failed to reissue access token: " + response.getStatusCode());
            return null;
        }
    }

    // 네이버 탈퇴
    public void NaverDelete(String accessToken)
    {
        log.info("네이버 삭제 접근");

        RestTemplate restTemplate = new RestTemplate();

        // oauth2 토큰이 만료 시 재 로그인
        if (accessToken == null) {
            log.info(" 엑세스 토큰이 없어염 ");
            return;
        }

        String url = NAVER_TOKEN_URL +
                "?service_provider=NAVER" +
                "&grant_type=delete" +
                "&client_id=" +
                NAVER_CLIENT_ID +
                "&client_secret=" +
                NAVER_CLIENT_SECRET +
                "&access_token=" +
                accessToken;

        UnlinkResponse response =
                restTemplate.getForObject(url, UnlinkResponse.class);

        if (response != null && !"success".equalsIgnoreCase(response.getResult())) {
            log.info("회원탈퇴가 제대로 되지않음");
        }
    }



    // 회원 탈퇴 로직(구글)
    public boolean revokeGoogleToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("token", accessToken);
        map.add("client_id", google_clientId);
        map.add("client_secret", google_clientSecret);


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity("https://oauth2.googleapis.com/revoke",
                    request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                // 연결 끊기 실패
                return false;
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            e.printStackTrace();
            return false;
        }
    }


    // 소셜 회원가입
    public void OAuth2Register(UserRegisterForm userRegisterForm) throws DataFormatException {
        Optional<Users> OptionalUsers = userRepository.findByEmail(userRegisterForm.getEmail());

        Users users;

        if(OptionalUsers.isEmpty())
        {
            throw new DataFormatException("해당 유저는 없어염");
        }
        else {
            users = OptionalUsers.get();
        }

        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setId(users.getId());
        userRequestDTO.setUsername(users.getUsername());
        userRequestDTO.setNickname(users.getNickname());
        userRequestDTO.setEmail(users.getEmail());
        userRequestDTO.setUserRole(UserRole.USER);
        userRequestDTO.setProviders(users.getProviders());
        userRequestDTO.setProviderIds(users.getProviderIds());
        userRequestDTO.setCreateDate(LocalDateTime.now());

        users = userRequestDTO.ModifytoEntity();
        userRepository.save(users);
    }
}
