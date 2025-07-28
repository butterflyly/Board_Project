package hello.project.BoardProject.Entity.Users;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
/*
 메세지 엔티티
 */
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Users sender; // 발신자
    @ManyToOne(cascade = CascadeType.ALL)
    private Users receiver; // 수신자

    private String title;
    private String content; // 메시지 내용
    private LocalDateTime sendTime; // 전송 시간

    private Long deleteSenderId;
    private String deleteSenderNickname;
    private Long deleteReceiverId;
    private String deleteReceiverNickname;

    @Builder.Default
    private Boolean SenderDeleteUsers = Boolean.FALSE;  // true 면 송신자는 못보게

    public void SenderDelete()
    {
        this.SenderDeleteUsers = true;
    }

    @Builder.Default
    private Boolean ReceiverDeleteUsers = Boolean.FALSE; // true 면 수신자는 못보게

    public void ReceiverDelete()
    {
        this.ReceiverDeleteUsers = true;
    }


    public void SenderDelete(Long id,String nickname)
    {
        deleteSenderId = id;
        deleteSenderNickname = nickname;
        sender = null;
    }

    public void ReceiverDelete(Long id,String nickname)
    {
        deleteReceiverId = id;
        deleteReceiverNickname = nickname;
        receiver = null;
    }

    public void SenderHardDelete(String nickname)
    {
        deleteSenderNickname = nickname;
        sender = null;
        deleteSenderId = null;
    }

    public void ReceiverHardDelete(String nickname)
    {
        deleteReceiverId = null;
        deleteReceiverNickname = nickname;
        receiver = null;
    }

    public void SenderReStore(Users users)
    {
        deleteSenderId = null;
        deleteSenderNickname = null;
        sender = users;
    }

    public void ReceiverReStore(Users users)
    {
        deleteReceiverId = null;
        deleteReceiverNickname = null;
        receiver = users;
    }

    public void SenderDeleteUserInfo(String nickname)
    {
        this.deleteSenderNickname = nickname;
    }

    public void ReceiverDeleteUserInfo(String nickname)
    {
        this.deleteReceiverNickname = nickname;
    }
}