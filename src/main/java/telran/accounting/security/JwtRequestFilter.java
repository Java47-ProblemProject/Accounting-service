package telran.accounting.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import telran.accounting.configuration.EmailEncryptionConfiguration;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.exceptions.ExceptionDto;
import telran.accounting.model.Profile;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final ProfileRepository profileRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        String encryptedEmail;
        Profile profile;
        try {
            String email = jwtTokenService.extractEmailFromToken(token);
            encryptedEmail = EmailEncryptionConfiguration.encryptAndEncodeUserId(email);
            if (!jwtTokenService.validateToken(token)) {
                ExceptionDto exceptionDto = new ExceptionDto(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", request);
                exceptionDto.setMessage("Authentication failed. Please provide a valid authentication token.");
                customAuthenticationEntryPoint.sendJsonResponse(response, exceptionDto);
                return;
            }
            profile = profileRepository.findById(encryptedEmail).orElse(null);
            if (profile == null) {
                ExceptionDto exceptionDto = new ExceptionDto(HttpStatus.FORBIDDEN.value(), "Forbidden", request);
                exceptionDto.setMessage("Access to this resource is forbidden for your current role or permissions.");
                customAuthenticationEntryPoint.sendJsonResponse(response, exceptionDto);
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        Set<String> roleStrings = jwtTokenService.extractRolesFromToken(token);
        Set<SimpleGrantedAuthority> authorities = roleStrings.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        UserDetails userDetails = new User(profile.getEmail(), "", authorities);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}

