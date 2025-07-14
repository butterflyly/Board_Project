package hello.project.BoardProject.Service;

import hello.project.BoardProject.DTO.Users.MessageResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Users.Message;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Repository.Users.MessageRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MessageService {


    private final MessageRepository messageRepository;


    private final UserRepository userRepository;



    public void sendMessage(UserResponseDTO sender,UserResponseDTO recevier ,
                            String title, String content) {

        Optional<Users> send = userRepository.findById(sender.getId());
        Optional<Users> recevie = userRepository.findById(recevier.getId());

        Message message = Message.builder().
        sender(send.get()).receiver(recevie.get()).
                title(title).content(content).sendTime(LocalDateTime.now()).
                build();

        messageRepository.save(message);
    }

    public Page<MessageResponseDTO> SendList(UserResponseDTO userResponseDTO, int page)
    {
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow();
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC,"send_Time"); //페이지 번호, 개수

        Page<Message> messageList = messageRepository.findAllBySenderAndSenderDeleteUsersFalse(users.getId(),pageable);

        Page<MessageResponseDTO> messageResponseDTOPage = messageList.map(m -> new MessageResponseDTO(m));

        return messageResponseDTOPage;
    }

    public MessageResponseDTO getMessageDTO(Long id) {

        Optional<Message> message = messageRepository.findById(id);

        if(message.isEmpty())
        {
            throw new DataNotFoundException("메세지가 없습니다.");
        }
        else {
            MessageResponseDTO messageResponseDTO = new MessageResponseDTO(message.get());
            return messageResponseDTO;
        }
    }

    public Message getMessage(Long id)
    {
        Optional<Message> message = messageRepository.findById(id);

        if(message.isEmpty())
        {
            throw new DataNotFoundException("메세지가 없습니다.");
        }
        else {
           return message.get();
        }
    }

    @Transactional
    public void sendDelete(Long id, String username) {

        Message message = getMessage(id);

        // case 1. 수신자가 탈퇴하지 않았거나 수신자가 완전탈퇴가 아닌 경우 => 메세지는 유지
        // case 2. 수신자 역시 완전탈퇴일 경우 => 메세지 엔티티 삭제
        // case 3. 수신자도 메세지를 삭제했을 경우 => 메세지 엔티티 삭제

        log.info("수신자 펄스관련 :" + message.getReceiverDeleteUsers());

        if(username.equals(message.getSender().getUsername()))
        {
            // case 1.
            if(message.getReceiver() != null || message.getDeleteReceiverId() != null)
            {
                if(message.getReceiverDeleteUsers())
                {
                    messageRepository.delete(message);
                }
                else {
                    message.SenderDelete();
                    messageRepository.save(message);
                }
            }
            // case 2.
            else if(message.getReceiver() == null && message.getDeleteReceiverId() == null)
            {
                messageRepository.delete(message);
            }
            else {
                // case 3.
                if(message.getReceiverDeleteUsers())
                {
                    messageRepository.delete(message);
                }
            }
        }
    }

    public Page<MessageResponseDTO> RecevieList(UserResponseDTO userResponseDTO,int page) {
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow();

        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC,"send_Time"); //페이지 번호, 개수


        Page<Message> messageList = messageRepository.findAllByReceiverAndReceiverDeleteUsersFalse(users.getId(),pageable);
        Page<MessageResponseDTO> messageResponseDTOPage = messageList.map(m -> new MessageResponseDTO(m));


        return messageResponseDTOPage;
    }

    public void ReceiveDelete(Long id, String username) {

        Message message = getMessage(id);

        // case 1. 송신자가 탈퇴하지 않았거나 송신자가 완전탈퇴가 아닌 경우 => 메세지는 유지
        // case 2. 송신자 역시 완전탈퇴일 경우 => 메세지 엔티티 삭제
        // case 3. 송신자도 메세지를 삭제했을 경우 => 메세지 엔티티 삭제

        if(username.equals(message.getReceiver().getUsername()))
        {
            // case 1.
            if(message.getSender() != null || message.getDeleteSenderId() != null)
            {
                if(message.getSenderDeleteUsers())
                {
                    messageRepository.delete(message);
                }
                else {
                    message.ReceiverDelete();
                    messageRepository.save(message);
                }
            }
            // case 2.
            else if(message.getSender() == null && message.getDeleteSenderId()== null)
            {
                messageRepository.delete(message);
            }

            else {
                // case 3.
                if(message.getSenderDeleteUsers())
                {
                    messageRepository.delete(message);
                }
            }
        }


    }

    public static void alertAndClose(HttpServletResponse response, String msg) {
        try {
            response.setContentType("text/html; charset=utf-8");
            PrintWriter w = response.getWriter();
            w.write("<script>alert('"+msg+"');history.go(-1);</script>");
            w.flush();
            w.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}