package telran.accounting.dto.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;

@Getter
public class ExceptionDto {
    private final String timestamp;
    private final int status;
    private final String error;
    @Setter
    private String message;
    @Setter
    private String path;

    public ExceptionDto(int status, String error, HttpServletRequest request) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
        this.error = error;
        this.message = "";
        this.path = request.getRequestURI();
    }
    public ExceptionDto(int status, String error) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
        this.error = error;
        this.message = "";
        this.path = "";
    }
}
