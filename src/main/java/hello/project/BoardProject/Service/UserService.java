package hello.project.BoardProject.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import hello.project.BoardProject.DTO.ChartData;
import hello.project.BoardProject.DTO.Users.UserRequestDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardNotVoter;
import hello.project.BoardProject.Entity.Board.BoardVoter;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Comment.CommentNotRecommend;
import hello.project.BoardProject.Entity.Comment.CommentRecommend;
import hello.project.BoardProject.Entity.Users.Message;
import hello.project.BoardProject.Entity.Users.UserRole;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Repository.Board.BoardNotVoterRepository;
import hello.project.BoardProject.Repository.Board.BoardRepository;
import hello.project.BoardProject.Repository.Board.BoardVoterRepository;
import hello.project.BoardProject.Repository.Board.DeleteBoardRepository;
import hello.project.BoardProject.Repository.Comment.CommentNotRecommendRepository;
import hello.project.BoardProject.Repository.Comment.CommentRecommendRepository;
import hello.project.BoardProject.Repository.Comment.CommentRepository;
import hello.project.BoardProject.Repository.Users.ImageRepository;
import hello.project.BoardProject.Repository.Users.MessageRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.Authenticator.RequestorType.SERVER;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final ImageRepository imageRepository;
    private final BoardRepository boardRepository;
    private final BoardVoterRepository boardVoterRepository;
    private final BoardNotVoterRepository boardNotVoterRepository;
    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;
    private final CommentRecommendRepository commentRecommendRepository;
    private final CommentNotRecommendRepository commentNotRecommendRepository;
    private final DeleteBoardRepository deleteBoardRepository;


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private static final String NAVER_URL = "https://nid.naver.com/oauth2.0/token";


    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;

    @Value("${spring.mail.username}")
    private String ADMIN_ADDRESS;


    private static int number;


    /*
     회원가입 로직
     */
    public void UserCreate(String username,String password,String email,String nickname,LocalDateTime createDate)
    {
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername(username);
        userRequestDTO.setPassword(passwordEncoder.encode(password));
        userRequestDTO.setEmail(email);
        userRequestDTO.setNickname(nickname);
        userRequestDTO.setCreateDate(createDate);

        // 관리자인지 아닌지 판별
        if(username =="admin")
        {
            userRequestDTO.setUserRole(UserRole.ADMIN);
        }
        else {
            userRequestDTO.setUserRole(UserRole.USER);
        }

        // 회원가입 저장
        Users users = userRequestDTO.toEntity();
        userRepository.save(users);

        // 프로필 이미지 자동지정
        UsersImage image = UsersImage.builder()
                .url("/profileImages/anonymous.png")
                .users(users)
                .build();

        imageRepository.save(image);
    }

    /*
     닉네임 중복체크
     */
    public boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /*
     유저 아이디 중복체크
    */
    public boolean checkUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /*
     이메일 중복체크
     */
    public boolean checkEmail(String email)
    {
        return userRepository.existsByEmail(email);
    }

    // 엔티티 데이터 조화
    public Users getUser(String username)
    {
        Optional<Users> users = this.userRepository.findByusername(username);
        if(users.isPresent())
        {
            return users.get();
        }
        else
        {
            throw new DataNotFoundException("user not found");
        }
    }

    public UserResponseDTO getUserDTO(Long userId)
    {
        Optional<Users> users = this.userRepository.findById(userId);
        if(users.isPresent())
        {
            UserResponseDTO userResponseDTO = getUserDTO(users.get().getUsername());
            return userResponseDTO;
        }
        else
        {
            throw new DataNotFoundException("user not found");
        }
    }

    public UserResponseDTO getUserNicknameDTO(String nickname)
    {
        Users users = this.userRepository.findByNickname(nickname);
        if(users != null)
        {
            UserResponseDTO userResponseDTO = getUserDTO(users.getUsername());
            return userResponseDTO;
        }
        else
        {
            return null;
        }
    }

    public Users getUserEmail(String email)
    {
        Optional<Users> users = this.userRepository.findByEmail(email);
        if(users.isPresent())
        {

            return users.get();
        }
        else
        {
            throw new DataNotFoundException("user not found");
        }
    }

    public UserResponseDTO getUserEmailDTO(String email)
    {
        Optional<Users> users = this.userRepository.findByEmail(email);
        if(users.isPresent())
        {
            UserResponseDTO userResponseDTO = getUserDTO(users.get().getUsername());
            return userResponseDTO;
        }
        else
        {
            throw new DataNotFoundException("user not found");
        }
    }

    /*
      유저 데이터 조회
     */
    public UserResponseDTO getUserDTO(String username)
    {
        Optional<Users> users = this.userRepository.findByusername(username);
        if(users.isPresent())
        {
            UserResponseDTO userResponseDTO = new UserResponseDTO(users.get());
            return userResponseDTO;
        }
        else
        {
            throw new DataNotFoundException("user not found");
        }
    }

    /*
     닉네임 데이터 수정
     */
    public void NicknameUpdate(String nickname,String username)
    {
        Users users = getUser(username);
        users.NicknameUpdate(nickname);
        userRepository.save(users);
    }


    @Transactional
    // 비밀번호 체크
    public boolean checkPassword(UserResponseDTO userResponseDTO, String checkPassword) {

        String realPassword = userResponseDTO.getPassword();
        boolean matches = passwordEncoder.matches(checkPassword, realPassword);
        return matches;
    }

    // 비밀번호 수정
    public void PWChange(UserResponseDTO user, String newPassword)
    {
        Users users = getUser(user.getUsername());
        users.PasswordUpdate(passwordEncoder.encode(newPassword));
        this.userRepository.save(users);
    }

    public void UserDelete(String username)
    {
        Users users = getUser(username);
        List<Board> boardList = boardRepository.findByUsers(users);
        UsersImage usersImage = imageRepository.findByUsers(users);
        List<Comment> comments = commentRepository.findByUsers(users);
        List<Message> senderList = messageRepository.findAllBySender(users);
        List<Message> receiverList = messageRepository.findAllByReceiver(users);
        List<BoardVoter> boardVoterList = boardVoterRepository.findByVoter(users);
        List<BoardNotVoter> boardNotVoterList = boardNotVoterRepository.findByNotVoter(users);
        List<CommentRecommend> commentRecommends = commentRecommendRepository.findByUser(users);
        List<CommentNotRecommend> commentNotRecommends = commentNotRecommendRepository.findByUser(users);
        List<Board> deleteBoardList = deleteBoardRepository.findAllByUsers(users.getId());

        log.info("딜리트 보드 사이즈 :" + deleteBoardList.size());


        if(!boardList.isEmpty())
        {
            for(Board board : boardList)
            {
                board.UserDelete(users.getId(),users.getNickname());
                boardRepository.save(board);
            }
        }

        if(!deleteBoardList.isEmpty())
        {
            log.info("User ID : "+ users.getId());
            log.info("유저 닉네임 : "+ users.getNickname());

            deleteBoardRepository.UserDelete(users.getId(),users.getNickname());

            log.info("User ID : "+ users.getId());
            log.info("유저 닉네임 : "+ users.getNickname());
        }

        if(usersImage != null)
        {
            usersImage.UserDelete(users.getId());
            imageRepository.save(usersImage);
        }

        if(!comments.isEmpty())
        {
            for(Comment comment : comments)
            {
                comment.UserDelete(users.getId(), users.getNickname());
                commentRepository.save(comment);
            }
        }

        if(!senderList.isEmpty())
        {
            for(Message message : senderList)
            {
                message.SenderDelete(users.getId(), users.getNickname()); // 임시로 false 넣음
                messageRepository.save(message);
            }
        }

        if(!receiverList.isEmpty())
        {
            for(Message message : receiverList)
            {
                message.ReceiverDelete(users.getId(), users.getNickname());
                messageRepository.save(message);
            }
        }

        if(!boardVoterList.isEmpty())
        {
            for(BoardVoter boardVoter : boardVoterList)
            {
                boardVoterRepository.delete(boardVoter);
            }
        }

        if(!boardNotVoterList.isEmpty())
        {
            for(BoardNotVoter boardNotVoter : boardNotVoterList)
            {
                boardNotVoterRepository.delete(boardNotVoter);
            }
        }

        if(!commentRecommends.isEmpty()){
            for(CommentRecommend commentRecommend : commentRecommends)
            {
                commentRecommendRepository.delete(commentRecommend);
            }
        }

        if(!commentNotRecommends.isEmpty())
        {
            for(CommentNotRecommend commentNotRecommend : commentNotRecommends)
            {
                commentNotRecommendRepository.delete(commentNotRecommend);
            }
        }


        users.Delete_Time(LocalDateTime.now());

        userRepository.save(users); // 삭제시간 저장
        userRepository.delete(users);
    }


    // 아이디정보 송신
    public void sendfindIdEmail(String email) {

        Users users = getUserEmail(email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(ADMIN_ADDRESS);
        message.setSubject(email + "님의 아이디정보 안내 메일입니다.");
        message.setText("안녕하세요 " + email + "님의 아이디는 [" + users.getUsername() + "] 입니다.");

        mailSender.send(message);
    }


    public void send_find_Password_Email(String email) {
        // 테스트를 위해 @test.com인 이메일은 발송하지 않음
        if (email.endsWith("@test.com"))
            return;

        UserResponseDTO users = getUserEmailDTO(email);
        String newPassword = PasswordGenerator.generateRandomPassword();
        PWChange(users, newPassword);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(ADMIN_ADDRESS);
        message.setSubject(email + "님의 임시 비밀번호 안내 메일입니다.");
        message.setText("안녕하세요 " + email + "님의 임시 비밀번호는 [" + newPassword + "] 입니다.");

        mailSender.send(message);
    }

    // 랜덤으로 숫자 생성
    public static void createNumber() {
        number = (int)(Math.random() * (90000)) + 100000; //(int) Math.random() * (최댓값-최소값+1) + 최소값
    }




    // 랜덤 비밀번호 생성
    public static class PasswordGenerator {
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";

        private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
        private static final int PASSWORD_LENGTH = 12;

        public static String generateRandomPassword() {
            if (PASSWORD_LENGTH < 1) throw new IllegalArgumentException("Password length must be at least 1");

            StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
            Random random = new SecureRandom();
            for (int i = 0; i < PASSWORD_LENGTH; i++) {
                int rndCharAt = random.nextInt(PASSWORD_ALLOW_BASE.length());
                char rndChar = PASSWORD_ALLOW_BASE.charAt(rndCharAt);
                sb.append(rndChar);
            }

            return sb.toString();
        }
    }



    public MimeMessage CreateMail(String mail) {
        createNumber();
        MimeMessage message = mailSender.createMimeMessage();

        try {
            message.setFrom(ADMIN_ADDRESS);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("이메일 인증");
            message.setText("이메일 인증번호는" + number +"입니다.");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public int sendMail(String mail) {
        MimeMessage message = CreateMail(mail);
        mailSender.send(message);

        return number;
    }

    // 관리자가 유저 리스트를 페이징처리해서 받음
    public Page<UserResponseDTO> UserPageList(int page)
    {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); //페이지 번호, 개수
        Page<Users> usersList = userRepository.findAll(pageable);
        Page<UserResponseDTO> userResponseDTOList =
                usersList.map(u -> new UserResponseDTO(u));

        return userResponseDTOList;
    }

    // 회원가입 차트
    public ChartData RegisterChart()
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthMinus = now.minusMonths(1);

        // 최근 한달간 회원가입한 유저 데이터
        List<Users> usersList = userRepository.findByCreateDateBetweenOrderByCreateDateAsc(oneMonthMinus,now);

        List<UserResponseDTO> userResponseDTOList =
                usersList.stream().map(u -> new UserResponseDTO(u)).collect(Collectors.toList());

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        Map<String, Integer> UsersCounts = new LinkedHashMap<>();

        for (UserResponseDTO userResponseDTO : userResponseDTOList) {
            String date = userResponseDTO.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            UsersCounts.put(date, UsersCounts.getOrDefault(date, 0) + 1);
        }

        for (String date : UsersCounts.keySet()) {
            labels.add(date);
            values.add(UsersCounts.get(date));
        }

        ChartData chartData = new ChartData(labels,values);

        return chartData;
    }


   public void NaverDelete(String accessToken)
   {

       RestTemplate restTemplate = new RestTemplate();

       // oauth2 토큰이 만료 시 재 로그인
       if (accessToken == null) {
           log.info(" 엑세스 토큰이 없어염 ");
           return;
       }

       String url = NAVER_URL +
               "?service_provider=NAVER" +
               "&grant_type=delete" +
               "&client_id=" +
               NAVER_CLIENT_ID +
               "&client_secret=" +
               NAVER_CLIENT_SECRET +
               "&access_token=" +
               accessToken;

       UnlinkResponse response = restTemplate.getForObject(url, UnlinkResponse.class);

       if (response != null && !"success".equalsIgnoreCase(response.getResult())) {
           log.info("회원탈퇴가 제대로 되지않음");
       }
   }

    /**
     * 네이버 응답 데이터
     */
    @Getter
    @RequiredArgsConstructor
    public static class UnlinkResponse {
        @JsonProperty("access_token")
        private final String accessToken;
        private final String result;
    }


    // 회원 탈퇴 로직 (예시)
    public boolean revokeGoogleToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        log.info("메소드 접근함?");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("token", accessToken);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {

            log.info("트라이문 들어옴?");

            ResponseEntity<String> response = restTemplate.postForEntity("https://oauth2.googleapis.com/revoke",
                    request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // 연결 끊기 성공
                log.info("성공함?");

                return true;
            } else {

                log.info("실패함?");

                // 연결 끊기 실패
                return false;
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            log.info("오류로 들어옴?");

            e.printStackTrace();
            return false;
        }
    }

}
