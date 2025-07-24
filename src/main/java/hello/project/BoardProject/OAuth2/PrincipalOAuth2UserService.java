package hello.project.BoardProject.OAuth2;


import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import hello.project.BoardProject.Entity.Users.UserRole;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import hello.project.BoardProject.OAuth2.Google.GoogleUserDetails;
import hello.project.BoardProject.OAuth2.Naver.NaverUserDetails;
import hello.project.BoardProject.Repository.Users.ImageRepository;
import hello.project.BoardProject.Repository.Users.OAuth2AccesTokenDataRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


/*시큐리티 설정(SecurityConfig)에서 loginProcessingUrl("/login") 해놔서
 * /login 요청오면 자동으로 UserDetailsService타입으로 IoC되어있는
 * loadUserByUsername 메서드가 실행되도록 되어있다.
 * */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrincipalOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest); // OAauh2의 정보를 가져옴

        log.info("getAttributes : {}",oAuth2User.getAttributes());

        // 네이버(혹은 구글) 서버에서 발급해주는 AccessToken 추출
        String oauth2AccessToken = userRequest.getAccessToken().getTokenValue();  // 소셜엑세스토큰 값 가져오기


        String provider = userRequest.getClientRegistration().getRegistrationId(); // 소셜사이트 정보 가져오기
        OAuth2UserInfo oAuth2UserInfo = null;

        // 뒤에 진행할 다른 소셜 서비스 로그인을 위해 구분 => 구글
        if(provider.equals("google")){
            log.info("구글 로그인");
            oAuth2UserInfo = new GoogleUserDetails(oAuth2User.getAttributes()); // 인터페이스에 구글 유저정보 넣어주기
        }
        else
        {
            log.info("네이버 로그인");
            oAuth2UserInfo = new NaverUserDetails(oAuth2User.getAttributes());
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String loginId = provider + "_" + providerId;
        String name = oAuth2UserInfo.getName()+"("+ provider + ")"; // 임시로 소셜 플랫폼 이름을 닉네임에 넣어줌
        UserRole role = UserRole.GUEST;

        Users findMember = userRepository.findByusername(loginId).orElse(null);
        Users member;

        if (findMember == null) {

            member = Users.builder()
                    .username(loginId)
                    .email(email)
                    .nickname(name)
                    .providers(provider)
                    .providerIds(providerId)
                    .userRole(role)
                    .build();
            userRepository.save(member);

            UsersImage image = UsersImage.builder()
                    .url("/profileImages/anonymous.png")
                    .users(member)
                    .build();

            imageRepository.save(image);

        } else{
            member = findMember;
        }

        // 임시로 엑세스 토큰을 데이터베이스화해서 저장
        Optional<OAuth2AccesTokenData> oAuth2AccesTokenData = oAuth2AccesTokenDataRepository.findByUsername(member.getUsername());

        // 토큰값이 비어있는 경우
        if(oAuth2AccesTokenData.isEmpty())
        {
            OAuth2AccesTokenData tokenData = new OAuth2AccesTokenData();
            tokenData.setUsername(member.getUsername());
            tokenData.setProvier(provider);
            tokenData.setToken(oauth2AccessToken);
            tokenData.setCreateDate(LocalDateTime.now());

            oAuth2AccesTokenDataRepository.save(tokenData);
        }

        return new PrincipalDetails(member, oAuth2User.getAttributes());
    }


}