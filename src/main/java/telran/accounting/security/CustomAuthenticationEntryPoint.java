package telran.accounting.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import telran.accounting.dto.exceptions.ExceptionDto;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ExceptionDto exceptionDto = new ExceptionDto(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", request);
        exceptionDto.setMessage("Authentication failed.");
        sendJsonResponse(response, exceptionDto);
    }

    public void sendJsonResponse(@NotNull HttpServletResponse response, ExceptionDto exceptionDto) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String exceptionDtoJson = objectMapper.writeValueAsString(exceptionDto);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write(exceptionDtoJson);
        writer.flush();
        writer.close();
    }
}
