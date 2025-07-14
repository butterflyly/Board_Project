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
import hello.project.BoardProject.Service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/*시큐리티 설정(SecurityConfig)에서 loginProcessingUrl("/login") 해놔서
 * /login 요청오면 자동으로 UserDetailsService타입으로 IoC되어있는
 * loadUserByUsername 메서드가 실행되도록 되어있다.
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;
  //  private final RedisUtil redisUtil;

    private final long ACCESS_TOKEN_EXPIRATION = 3600 * 1000;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> service = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = service.loadUser(userRequest); // OAauh2의 정보를 가져옴

        log.info("getAttributes : {}",oAuth2User.getAttributes());


        // 카카오(혹은 구글) 서버에서 발급해주는 AccessToken 추출
        String oauth2AccessToken = userRequest.getAccessToken().getTokenValue();  // 소셜엑세스토큰 값 가져오기
        Map<String, Object> originAttributes = oAuth2User.getAttributes(); // OAuth2User의 attribute


        String provider = userRequest.getClientRegistration().getRegistrationId(); // 소셜사이트 정보 가져오기
        OAuth2UserInfo oAuth2UserInfo = null;

        // OAuthAttributes: OAuth2User의 attribute를 서비스 유형에 맞게 담아줄 클래스
      //  OAuthAttributes attributes = OAuthAttributes.of(registrationId, originAttributes);

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
        String name = oAuth2UserInfo.getName();
        UserRole role = UserRole.USER;

        Users findMember = userRepository.findByusername(loginId).orElse(null);
        Users member;

        if (findMember == null) {
            member = Users.builder()
                    .username(loginId)
                    .nickname(name)
                    .email(email)
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



        /*레디스 소셜 로그인 토큰 저장*/
       // redisService.setValuesWithTimeout("AT(oauth2):" + loginId , oauth2AccessToken, ACCESS_TOKEN_EXPIRATION);


        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new PrincipalDetails(member, oAuth2User.getAttributes());
    }


}