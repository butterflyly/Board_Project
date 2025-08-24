package hello.project.BoardProject.OAuth2;


import hello.project.BoardProject.Entity.Users.OAuth2AccesTokenData;
import hello.project.BoardProject.Entity.Users.UserRole;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import hello.project.BoardProject.OAuth2.Google.GoogleUserDetails;
import hello.project.BoardProject.OAuth2.Naver.NaverUserDetails;
import hello.project.BoardProject.Repository.Users.DeleteUserRepository;
import hello.project.BoardProject.Repository.Users.ImageRepository;
import hello.project.BoardProject.Repository.Users.OAuth2AccesTokenDataRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import hello.project.BoardProject.Service.Users.Delete_UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/*시큐리티 설정(SecurityConfig)에서 loginProcessingUrl("/login") 해놔서
 * /login 요청오면 자동으로 UserDetailsService타입으로 IoC되어있는
 * loadUserByUsername 메서드가 실행되도록 되어있다.
 * */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrincipalOAuth2UserService  extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final OAuth2AccesTokenDataRepository oAuth2AccesTokenDataRepository;
    private final DeleteUserRepository deleteUserRepository;
    private final Delete_UserService deleteUserService;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest); // OAauh2의 정보를 가져옴

        log.info("getAttributes : {}",oAuth2User.getAttributes());

        // 네이버(혹은 구글) 서버에서 발급해주는 AccessToken 추출
        String access = userRequest.getAccessToken().getTokenValue();
        OAuth2AccessToken.TokenType tokenType = userRequest.getAccessToken().getTokenType();
        log.info("액세스 : "+ tokenType);

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
        Users deleteFindMember = deleteUserRepository.findByusername(loginId).orElse(null);
        Users member;

        // 그냥 아얘 없는 회원 0 0
        // USER_ROLE 을 GUEST 로 설정하고 회원가입
        if (findMember == null && deleteFindMember == null) {
            member = Users.builder()
                    .username(loginId)
                    .email(email)
                    .nickname(name)
                    .providers(provider)
                    .providerIds(providerId)
                    .userRole(role)
                    .build();
            userRepository.save(member);

            // 기본이미지 넣기
            UsersImage image = UsersImage.builder()
                    .url("/profileImages/anonymous.png")
                    .users(member)
                    .build();

            imageRepository.save(image);
        }
        // 소프트 삭제된 유저가 있는 경우 0 1
        else if(deleteFindMember != null)
        {
            deleteFindMember.Deleted_False();
            deleteUserRepository.save(deleteFindMember);

            deleteUserService.User_ReStore(deleteFindMember.getId());

            member = Users.builder()
                    .username(deleteFindMember.getUsername())
                    .email(deleteFindMember.getEmail())
                    .nickname(deleteFindMember.getNickname())
                    .providers(deleteFindMember.getProviders())
                    .providerIds(deleteFindMember.getProviderIds())
                    .userRole(role)
                    .build();
            userRepository.save(member);
        }
        // 1 0
        // 1 1 인 경우는 이론상 나올수 없으므로 1 0인 경우
        else{
            member = findMember;
        }

        Optional<OAuth2AccesTokenData> OptionOAuth2AccesTokenData = oAuth2AccesTokenDataRepository.findByUsername(member.getUsername());

        OAuth2AccesTokenData oAuth2AccesTokenData;

        // 엔티티가 존재할 경우
        if(!OptionOAuth2AccesTokenData.isEmpty())
        {
            oAuth2AccesTokenData = OptionOAuth2AccesTokenData.get();
            // 엑세스 토큰이 없다면 액세스 토큰 생성
            if(oAuth2AccesTokenData.getToken() == null)
            {
                oAuth2AccesTokenData.setToken(access);
                oAuth2AccesTokenData.setAccessCreateDate(LocalDateTime.now());
            }
        }

        // 엔티티가 존재하지 않을 경우 엔티티 생성
        else {
            oAuth2AccesTokenData = new OAuth2AccesTokenData();
            oAuth2AccesTokenData.setToken(access);
            oAuth2AccesTokenData.setUsername(member.getUsername());
            oAuth2AccesTokenData.setAccessCreateDate(LocalDateTime.now());
            oAuth2AccesTokenData.setProvier(providerId);
            oAuth2AccesTokenDataRepository.save(oAuth2AccesTokenData);
        }

        // 사용자 정보를 세션에 저장
        SecurityContextHolder.getContext().
                setAuthentication(new
                        UsernamePasswordAuthenticationToken(member.getUserRole(),
                        member, null));

        return new PrincipalDetails(member, oAuth2User.getAttributes());

    }


}

