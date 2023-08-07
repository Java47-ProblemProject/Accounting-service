package telran.accounting.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProfileExistsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2363686790586241857L;
}
