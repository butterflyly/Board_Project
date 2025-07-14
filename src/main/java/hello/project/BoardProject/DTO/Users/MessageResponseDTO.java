package hello.project.BoardProject.DTO.Users;

import hello.project.BoardProject.Entity.Users.Message;
import hello.project.BoardProject.Entity.Users.Users;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponseDTO {

    private Long id;
    private Users sender;
    private Users receiver;
    private String Receivernickname;
    private String Sendernickname;

    private String title;
    private String content; // 메시지 내용
    private LocalDateTime sendTime; // 전송 시간

    private Long deleteSenderId;
    private String deleteSenderNickname;
    private Long deleteReceiverId;
    private String deleteReceiverNickname;

    public MessageResponseDTO(Message message)
    {
        this.id = message.getId();

        // 수신자가 탈퇴하지 않은 경우
        if(message.getReceiver() != null)
        {
            this.Receivernickname = message.getReceiver().getNickname();
        }
        // 수신자가 소프트 탈퇴한 경우
        else if (message.getReceiver() == null && message.getDeleteReceiverId() !=null) {
            this.Receivernickname = message.getDeleteReceiverNickname()+"(소프트 탈퇴 유저)";
        }
        // 수신자가 완전탈퇴한 경우
        else {
            this.Receivernickname = message.getDeleteReceiverNickname()+ "(수신자가 완전탈퇴 했습니다.)";
        }

        if(message.getSender() != null)
        {
            this.Sendernickname = message.getSender().getNickname();
        }
        else if(message.getSender() == null && message.getDeleteSenderId() != null)
        {
            this.Sendernickname = message.getDeleteSenderNickname() +"(소프트 탈퇴 유저)";
        }
        else {
            this.Sendernickname = message.getDeleteSenderNickname() +"(송신자가 완전탈퇴 했습니다)";
        }

        this.sender = message.getSender();
        this.receiver = message.getReceiver();
        this.title = message.getTitle();
        this.content = message.getContent();
        this.sendTime = message.getSendTime();
        this.deleteSenderId = message.getDeleteSenderId();
        this.deleteReceiverId = message.getDeleteReceiverId();
        this.deleteSenderNickname = message.getDeleteSenderNickname();
        this.deleteReceiverNickname = message.getDeleteReceiverNickname();
    }
}
