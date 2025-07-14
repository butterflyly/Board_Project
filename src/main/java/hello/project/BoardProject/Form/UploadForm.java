package hello.project.BoardProject.Form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UploadForm {

    private List<MultipartFile> files;
}
