package hello.project.BoardProject.Controller;

import hello.project.BoardProject.DTO.Users.MessageResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Form.Users.MessageForm;
import hello.project.BoardProject.Service.MessageService;
import hello.project.BoardProject.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/message")
@PreAuthorize("isAuthenticated()")

public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    // 메세지 송신
    @GetMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public String send(Model model,Principal principal)
    {
        MessageForm messageForm = new MessageForm();
        model.addAttribute("messageForm",messageForm);
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());
        model.addAttribute("sender_nickname",userResponseDTO.getNickname());
        return "message/send";
    }

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public String send(Principal principal,
            @Valid @ModelAttribute MessageForm messageForm,
                       BindingResult bindingResult, HttpServletResponse response) {

        if(bindingResult.hasErrors())
        {
            return "message/send";
        }

        UserResponseDTO send_user = userService.getUserDTO(principal.getName()); // 송신 유저
        UserResponseDTO receiver_user = userService.getUserNicknameDTO(messageForm.getReceiverNickname()); // 수신자 유저

        if(receiver_user == null)
        {
            alertAndClose(response,"수신자가 존재하지 않습니다.");
        }
        else if (send_user.getId() == receiver_user.getId()) {
            alertAndClose(response,"송신자와 수신자가 같습니다.");
        }
        else{
            messageService.sendMessage(send_user,receiver_user,messageForm.getTitle(),
                    messageForm.getContent());
        }
        // 쪽지 전송 성공 메시지 표시 (or 리다이렉트)
        return "redirect:/";
    }


    // 송신 쪽지 목록
    @GetMapping("/send/list")
    @PreAuthorize("isAuthenticated()")
    public String sendList(Principal principal,Model model,@RequestParam(defaultValue = "0") int page)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());

        Page<MessageResponseDTO> messageList = messageService.SendList(userResponseDTO,page);

        model.addAttribute("paging", messageList);
        model.addAttribute("messageType",0);

        return "message/send_list";
    }

    // 송신 쪽지 상세
    @GetMapping("/send/detail/{id}")
    @PreAuthorize("isAuthenticated()")
    public String sendDetail(@PathVariable Long id,Model model,Principal principal,HttpServletResponse httpServletResponse)
    {
        MessageResponseDTO message = messageService.getMessageDTO(id);

        // case 1. 송신자와 수신자가 둘다 삭제함
        // case 2. 송신자만 삭제함
        // case 3. 수신자만 삭제함
        // case 4. 송신자와 수신자 둘다 삭제안함

        //  DeleteSender 가 트루면 송신자가 메세지를 삭제했다는 뜻이므로 송신자가 없는 것으로 처리

        // case 1,2. 송신자가 삭제됨
        if(message.getDeleteSenderId() != null)
        {
            alertAndClose(httpServletResponse,"접근 권한이 없습니다.");
        }
        // 송신자랑 접속유저가 일치하지 않으면 당연히 접근 권한이 없음
        // case 3,4 : 수신자가 삭제되어도 송신자는 볼 수 있게끔
        else if(!message.getSender().getUsername().equals(principal.getName()))
        {
            alertAndClose(httpServletResponse,"접근 권한이 없습니다.");
        }
        else
        {
            model.addAttribute("message" , message);
        }

        return "message/send_detail";
    }

    // 송신 쪽지 삭제
    @GetMapping("/send/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String sendDelete(@PathVariable Long id,Principal principal)
    {
        // 삭제라고는 했지만 진짜 삭제하면 수신자가 받은 쪽지까지 사라지므로
        // 송신자의 목록에서만 사라지게끔
        messageService.sendDelete(id,principal.getName());

        return "redirect:/message/send/list";
    }


    // 수신 쪽지 목록
    @GetMapping("/recevie/list")
    @PreAuthorize("isAuthenticated()")
    public String RecevieList(Principal principal,Model model,@RequestParam(defaultValue = "0") int page)
    {
        UserResponseDTO userResponseDTO = userService.getUserDTO(principal.getName());

        Page<MessageResponseDTO> messageList = messageService.RecevieList(userResponseDTO,page);

        model.addAttribute("paging", messageList);
        model.addAttribute("messageType",1);

        return "message/receive_list";
    }

    // 수신 쪽지 상세
    @GetMapping("/receive/detail/{id}")
    @PreAuthorize("isAuthenticated()")
    public String ReceiveDetail(@PathVariable Long id,Model model,Principal principal,HttpServletResponse httpServletResponse)
    {
        MessageResponseDTO message = messageService.getMessageDTO(id);

        // case 1. 송신자와 수신자가 둘다 삭제함
        // case 2. 송신자만 삭제함
        // case 3. 수신자만 삭제함
        // case 4. 송신자와 수신자 둘다 삭제안함

        //  DeleteSender 가 트루면 송신자가 메세지를 삭제했다는 뜻이므로 송신자가 없는 것으로 처리

        // case 1,3. 수신자가 삭제되거나 삭제함
        if(message.getDeleteReceiverId() != null)
        {
            alertAndClose(httpServletResponse,"접근 권한이 없습니다.");
        }

        // 송신자랑 접속유저가 일치하지 않으면 당연히 접근 권한이 없음
        // case 2,4 : 송신자가 삭제되어도 수신자는 볼 수 있게끔
        else if(!message.getReceiver().getUsername().equals(principal.getName()))
        {
            alertAndClose(httpServletResponse,"접근 권한이 없습니다.");
        }
        else
        {
            model.addAttribute("message" , message);
        }

        return "message/receive_detail";
    }

    // 수신 쪽지 삭제
    @GetMapping("/receive/delete/{id}")
    @PreAuthorize("isAuthenticated()")

    public String ReceiveDelete(@PathVariable Long id,Principal principal)
    {
        // 삭제라고는 했지만 진짜 삭제하면 수신자가 받은 쪽지까지 사라지므로
        // 송신자의 목록에서만 사라지게끔
        messageService.ReceiveDelete(id,principal.getName());

        return "redirect:/message/recevie/list";
    }

    //알림창 띄운 후 뒤로 이동
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