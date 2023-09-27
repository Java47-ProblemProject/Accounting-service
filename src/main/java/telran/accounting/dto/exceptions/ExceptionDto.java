package telran.accounting.dto.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

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
        ZoneId jerusalemZone = ZoneId.of("Asia/Jerusalem");
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC).atZone(jerusalemZone).toString();
        this.status = status;
        this.error = error;
        this.message = "";
        this.path = request.getRequestURI();
    }
    public ExceptionDto(int status, String error) {
        ZoneId jerusalemZone = ZoneId.of("Asia/Jerusalem");
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC).atZone(jerusalemZone).toString();
        this.status = status;
        this.error = error;
        this.message = "";
        this.path = "";
    }
}
