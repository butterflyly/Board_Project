package hello.project.BoardProject.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 엔티티를 찾을 수 없는 경우
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "entity not found")
public class DataNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // 오류 메세지
    public DataNotFoundException(String message) {
        super(message);
    }
}