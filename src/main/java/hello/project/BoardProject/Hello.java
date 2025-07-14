package hello.project.BoardProject;

import hello.project.BoardProject.Form.UploadForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class Hello {

    private final RedisProperties redisProperties;

    @GetMapping("/")
    public String hello()
    {
        log.info("Host :" + redisProperties.getHost());
        log.info("Port : " + redisProperties.getPort());

        return "redirect:/board/list/qna";
    }

    @GetMapping("/test")
    public String test()
    {
        return "test";
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("uploadForm", new UploadForm());
        return "uploadForm"; // 폼 뷰 이름
    }

    @PostMapping("/upload")
    public String handleFileUpload(@ModelAttribute UploadForm uploadForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // 오류 발생 시, 오류 메시지와 폼 데이터 유지
            model.addAttribute("uploadForm", uploadForm);
            return "uploadForm";
        }

        List<MultipartFile> files = uploadForm.getFiles();

        // 파일 처리 로직 (예: 파일 저장)
        if(files != null && !files.isEmpty()){
            for(MultipartFile file : files){
                if(!file.isEmpty()){
                    // 파일 저장 로직 구현
                    System.out.println("파일명: " + file.getOriginalFilename());
                }
            }
        }


        return "redirect:/"; // 성공 뷰
    }

}
