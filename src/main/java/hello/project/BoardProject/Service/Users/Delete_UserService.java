package hello.project.BoardProject.Service.Users;


import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.ChartData;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardRead;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.Message;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Repository.Board.BoardReadRepository;
import hello.project.BoardProject.Repository.Board.BoardRepository;
import hello.project.BoardProject.Repository.Board.DeleteBoardRepository;
import hello.project.BoardProject.Repository.Comment.CommentRepository;
import hello.project.BoardProject.Repository.Users.DeleteUserRepository;
import hello.project.BoardProject.Repository.Users.ImageRepository;
import hello.project.BoardProject.Repository.Users.MessageRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/*
 소프트 삭제 유저 서비스 로직
 */
public class Delete_UserService {

    private final DeleteUserRepository deleteUserRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;
    private final DeleteBoardRepository deleteBoardRepository;
    private final UserRepository userRepository;
    private final BoardReadRepository boardReadRepository;
    private final ImageRepository imageRepository;


    // 유저 리스트 페이징 데이터 리턴
    public Page<UserResponseDTO> UserList(int page)
    {
        Pageable pageable;
        pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC,"user_delete_create_date")); //페이지 번호, 개수
        Page<Users> usersList = deleteUserRepository.findAll(pageable);
        Page<UserResponseDTO> userResponseDTOList = usersList.map(u -> new UserResponseDTO(u));

        return userResponseDTOList;
    }

    // 엔티티 데이터 조화
    public Users getUser(String username)
    {
        Optional<Users> users = this.deleteUserRepository.findByusername(username);
        if(users.isPresent())
        {
            return users.get();
        }
        else
        {
            throw new DataNotFoundException("user not found");
        }
    }

    // 상세 유저 데이터 조회
    public UserResponseDTO getUserDTO(Long userId)
    {
        Optional<Users> users = this.deleteUserRepository.findById(userId);
        if(users.isPresent())
        {
            UserResponseDTO userResponseDTO = new UserResponseDTO(users.get());
            return userResponseDTO;
        }
        else
        {
            return null;
        }
    }


    // 유저 아이디 중복체크
    public boolean checkUsername(String username)
    {
        if(deleteUserRepository.existsByUsername(username) > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // 유저 닉네임 중복체크
    public boolean checkNickname(String nickname) {
        if(deleteUserRepository.existsByNickname(nickname) > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    // 유저 이메일 중복체크
    public boolean checkEmail(String email)
    {
        if(deleteUserRepository.existsByEmail(email) > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    // 최근 작성한 댓글 5개 가져오기
    public List<CommentResponseDTO> getCommentTop5LatestByUser(UserResponseDTO userResponseDTO) {
        Users users = deleteUserRepository.findByusername(userResponseDTO.
                getUsername()).orElseThrow(() -> new IllegalArgumentException("코멘트를 단 유저가 없음"));
        List<Comment> commentList = commentRepository.findTop5ByUsersOrderByCreateDateDesc(users);
        List<CommentResponseDTO> commentResponseDTOList =
                commentList.stream().map(b -> new CommentResponseDTO(b)).
                        collect(Collectors.toList());

        return commentResponseDTOList;
    }


    // 유저가 작성한 게시글 페이징 데이터 가져오기
    public Page<BoardResponseDTO> getPersonalBoardList(int page,String username,int category)
    {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("create_Date"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); //페이지 번호, 개수
        Users users = deleteUserRepository.findByusername(username).orElseThrow(()->new IllegalArgumentException("해당 유저없음"));
        Page<Board> board = boardRepository.findByCategoryAndDelete_User_Id(category,users.getId(), pageable);

        Page<BoardResponseDTO> boardResponseDtos = board.map(b ->new BoardResponseDTO(b));
        return boardResponseDtos;
    }

    // 유저가 작성한 댓글 페이징 데이터 가져오기
    public Page<CommentResponseDTO> getPersonalCommentList(int page, String username) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("create_Date"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); //페이지 번호, 개수

        Users users = deleteUserRepository.findByusername(username).orElseThrow(() -> new IllegalArgumentException("해당 유저없음"));
        Page<Comment> commentPage = commentRepository.findAllByDelete_User_Id(users.getId(), pageable);

        Page<CommentResponseDTO> commentResponseDTOPage =
                commentPage.map(b -> new CommentResponseDTO(b));
        return commentResponseDTOPage;

    }

    // 유저 복원하기
    public void User_ReStore(Long id) {
        UserResponseDTO userResponseDTO = getUserDTO(id);
        Users users = getUser(userResponseDTO.getUsername());

        users.Deleted_False();
        deleteUserRepository.save(users);


        List<Board> boardList = boardRepository.findByDelete_User_Id(users.getId());
        List<Comment> commentList = commentRepository.findByDelete_User_Id(users.getId());
        List<Message> senderList = messageRepository.findAllByDeleteSenderId(users.getId());
        List<Message> recevierList = messageRepository.findAllByDeleteReceiverId(users.getId());

        for(Board board : boardList)
        {
            if(Objects.equals(board.getDelete_user_id(), id))
            {
                board.UserReStore(users);
                boardRepository.save(board);
            }
        }

        for(Comment comment : commentList)
        {
            if(Objects.equals(comment.getDelete_user_id(), id))
            {
                comment.UserReStore(users);
                commentRepository.save(comment);
            }
        }

        for(Message message : senderList)
        {
            message.SenderReStore(users);
            messageRepository.save(message);
        }

        for(Message message : recevierList)
        {
            message.ReceiverReStore(users);
            messageRepository.save(message);

        }

    }

    // 소셜유저 완전탈퇴
    public void OAuth2Delete(String username)
    {
        Optional<Users> usersOptional = deleteUserRepository.findByusernameOAuth2(username);

        Users users;
        if(!usersOptional.isEmpty())
        {
            users = usersOptional.get();
        }
        else {
            return;
        }

        List<Board> boardList = boardRepository.findByUsers(users); // 게시글 리스트 가져오기
        List<Board> deleteBoardList = deleteBoardRepository.findALlByDelete_User_Id(users.getId()); // 유저가 삭제된 삭제 게시글 가져오기
        List<Board> deleteBoardList2 = deleteBoardRepository.findAllByUsers(users.getId()); // 삭제된 게시글 보기
        List<Comment> commentList = commentRepository.findByUsers(users);
        List<Message> senderList = messageRepository.findAllBySender(users);
        List<Message> receiverList = messageRepository.findAllByReceiver(users);
        List<BoardRead> boardReadList = boardReadRepository.findByUsers(users);

        UsersImage image = imageRepository.findByUsers(users);

        imageRepository.delete(image);

        List<UsersImage> deleteImage = imageRepository.findByDelete_User_Id(users.getId());

        if(!deleteImage.isEmpty())
        {
            for(UsersImage usersImage : deleteImage)
            {
                log.info("USERIMAGE ID : " + usersImage.getId());
                imageRepository.delete(usersImage);
            }
        }

        for(Board board : boardList)
        {
            boardRepository.delete(board);
        }

        for(Board board : deleteBoardList)
        {
            deleteBoardRepository.deleteById(board.getId());
        }

        for(Board board : deleteBoardList2)
        {
            deleteUserRepository.deleteById(board.getId());
        }

        for(Comment comment :commentList)
        {
            commentRepository.delete(comment);
        }

        for(BoardRead boardRead : boardReadList)
        {
            boardReadRepository.delete(boardRead);
        }


        for(Message message : senderList)
        {
            message.SenderHardDelete(users.getNickname());
            // 수신자가 하드탈퇴하였거나 수신자가 메세지를 삭제한 경우
            if((message.getReceiver() == null && message.getDeleteReceiverId() == null) || message.getReceiverDeleteUsers())
            {
                messageRepository.delete(message);
            }
            // 수신자가 하드탈퇴하지도 메세지를 삭제하지도 않은 경우
            else {
                messageRepository.save(message);
            }
        }

        for(Message message : receiverList)
        {
            message.ReceiverHardDelete(users.getNickname());

            // 송신자가 하드탈퇴하였거나 송신자가 메세지를 삭제한 경우
            if((message.getSender() == null && message.getDeleteSenderId() == null) || message.getSenderDeleteUsers())
            {
                messageRepository.delete(message);
            }
            else {
                messageRepository.save(message);
            }
        }

        userRepository.deleteById(users.getId());
    }

    // 유저 하드삭제
    public void Hard_Delete(Long id)
    {
        UserResponseDTO userResponseDTO = getUserDTO(id);
        Users users = getUser(userResponseDTO.getUsername());

        List<Board> boardList = boardRepository.findByDelete_User_Id(users.getId());
        List<Board> deleteBoardList = deleteBoardRepository.findALlByDelete_User_Id(users.getId());

        List<Comment> commentList = commentRepository.findByDelete_User_Id(users.getId());
        List<Message> senderList = messageRepository.findAllByDeleteSenderId(users.getId());
        List<Message> recevierList = messageRepository.findAllByDeleteReceiverId(users.getId());

        List<UsersImage> deleteImage = imageRepository.findByDelete_User_Id(users.getId());

        if(!deleteImage.isEmpty())
        {
            for(UsersImage usersImage : deleteImage)
            {
                imageRepository.delete(usersImage);

            }
        }


        for(Board board : boardList)
        {
            board.User_Hard_Delete(users.getNickname());
            boardRepository.save(board);
        }

        for(Board board : deleteBoardList)
        {
            deleteBoardRepository.UserHardDelete(board.getId(), users.getId(),users.getNickname());
        }

        for(Comment comment :commentList)
        {
            comment.UserDelete();
            commentRepository.save(comment);
        }


        for(Message message : senderList)
        {
            message.SenderHardDelete(users.getNickname());
            // 수신자가 하드탈퇴하였거나 수신자가 메세지를 삭제한 경우
            if((message.getReceiver() == null && message.getDeleteReceiverId() == null) || message.getReceiverDeleteUsers())
            {
                messageRepository.delete(message);
            }
            // 수신자가 하드탈퇴하지도 메세지를 삭제하지도 않은 경우
            else {
                messageRepository.save(message);
            }
        }

        for(Message message : recevierList)
        {
            message.ReceiverHardDelete(users.getNickname());

            // 송신자가 하드탈퇴하였거나 송신자가 메세지를 삭제한 경우
            if((message.getSender() == null && message.getDeleteSenderId() == null) || message.getSenderDeleteUsers())
            {
                messageRepository.delete(message);
            }
            else {
                messageRepository.save(message);
            }
        }

        deleteUserRepository.deleteById(users.getId());

    }


    // 유저 데이터 리스트 로직
    public List<UserResponseDTO> getList() {

       List<Users> usersList = deleteUserRepository.findAll();
       List<UserResponseDTO> userResponseDTOList = usersList.stream().map(u -> new UserResponseDTO(u)).collect(Collectors.toList());

        return userResponseDTOList;
    }

    // 소프트 삭제(회원탈퇴) 한 유저 차트
    public ChartData DeleteChart() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthMinus = now.minusMonths(1);

        // 최근 한달간 회원탈퇴한 유저 데이터
        List<Users> usersList = deleteUserRepository.
                findByDeleteUsers(oneMonthMinus,now);

        Collections.sort(usersList, Comparator.comparing(Users :: getUser_delete_createDate));

        List<UserResponseDTO> userResponseDTOList =
                usersList.stream().map(u -> new UserResponseDTO(u)).collect(Collectors.toList());

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        Map<String, Integer> UsersCounts = new LinkedHashMap<>();

        for (UserResponseDTO userResponseDTO : userResponseDTOList) {
            String date = userResponseDTO.getUser_delete_createDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            UsersCounts.put(date, UsersCounts.getOrDefault(date, 0) + 1);
        }

        for (String date : UsersCounts.keySet()) {
            labels.add(date);
            values.add(UsersCounts.get(date));
        }

        ChartData chartData = new ChartData(labels,values);

        return chartData;
    }

    // 회원 수정 시 외래키(였던) 엔티티들 수정
    public void deleteUserInfo(UserResponseDTO userResponseDTO,String nickname) {

        Users users = deleteUserRepository.findByusername(userResponseDTO.getUsername()).orElseThrow();

        List<Board> boardList = boardRepository.findByDelete_User_Id(users.getId());
        List<Comment> commentList = commentRepository.findByDelete_User_Id(users.getId());
        List<Message> senderList = messageRepository.findAllByDeleteSenderId(users.getId());
        List<Message> receiverList = messageRepository.findAllByDeleteReceiverId(users.getId());


        for(Board board : boardList)
        {
            board.DeleteUserInfo(nickname);

            boardRepository.save(board);
        }

        for(Comment comment : commentList)
        {
            comment.DeleteUserInfo(nickname);

            commentRepository.save(comment);
        }

        for(Message message : senderList)
        {
            message.SenderDeleteUserInfo(nickname);

            messageRepository.save(message);
        }

        for(Message message : receiverList)
        {
            message.ReceiverDeleteUserInfo(nickname);

            messageRepository.save(message);
        }

        deleteUserRepository.save(users.getId(), nickname);
    }
}
