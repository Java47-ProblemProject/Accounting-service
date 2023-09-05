package telran.accounting.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import telran.accounting.dto.exceptions.ExceptionDto;
import telran.accounting.dto.exceptions.ProfileExistsException;

import java.util.NoSuchElementException;

@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(ProfileExistsException.class)
    public ResponseEntity<Object> handleProfileExistsException(ProfileExistsException ex) {
        ExceptionDto exceptionDto = new ExceptionDto(HttpStatus.BAD_REQUEST.value(), "Bad Request");
        exceptionDto.setMessage("Profile with such email already exists.");
        exceptionDto.setPath("/user/registration");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionDto);
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex) {
        ExceptionDto exceptionDto = new ExceptionDto(HttpStatus.BAD_REQUEST.value(), "Bad Request");
        exceptionDto.setMessage("Profile with such email doesn't exists.");
        exceptionDto.setPath("/user/login");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionDto);
    }
}
