package hello.project.BoardProject.Repository.Users;

import hello.project.BoardProject.Entity.Users.Message;
import hello.project.BoardProject.Entity.Users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {




    List<Message> findAllBySender(Users users);

    List<Message> findAllByReceiver(Users users);

    List<Message> findAllByDeleteSenderId(Long id);

    List<Message> findAllByDeleteReceiverId(Long id);

    @Query(value = "select * from message m where (m.sender_user_id =:user_id) and m.sender_delete_users = false",nativeQuery = true)
    Page<Message> findAllBySenderAndSenderDeleteUsersFalse(@Param("user_id") Long user_id, Pageable pageable);

    @Query(value = "select * from message m where (m.receiver_user_id =:user_id) and m.receiver_delete_users = false",nativeQuery = true)
    Page<Message> findAllByReceiverAndReceiverDeleteUsersFalse(@Param("user_id") Long user_id, Pageable pageable);
}