package telran.accounting.security;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import telran.accounting.configuration.EmailEncryptionConfiguration;
import telran.accounting.model.Profile;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private static final Duration JWT_TOKEN_VALIDITY = Duration.ofMinutes(240);
    private final Logger logger = Logger.getLogger(JwtTokenService.class.getName());
    @Value("${jwt.secret-key}")
    private String jwtSecretKey;
    private SecretKey jwtSecret;
    private final Map<String, String> userTokenCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        byte[] secretBytes = jwtSecretKey.getBytes();
        jwtSecret = new SecretKeySpec(secretBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(Profile profile) {
        Instant now = Instant.now();
        String email = profile.getEmail();
        if (userTokenCache.containsKey(email)) {
            logger.info("Generated token for user " + email + ": " + userTokenCache.get(email));
            return userTokenCache.get(email);
        }
        String token = Jwts.builder()
                .setSubject(email)
                .claim("roles", profile.getRoles())
                .setIssuer("app")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(JWT_TOKEN_VALIDITY)))
                .signWith(jwtSecret)
                .compact();
        logger.info("Generated token for user " + email + ": " + token);
        userTokenCache.put(email, token);
        return token;
    }

    public String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            String encryptedEmail = claims.getSubject();
            if (claims.getExpiration().before(new Date())) {
                logger.log(Level.WARNING, "Token expired");
                return null;
            }
            return EmailEncryptionConfiguration.decryptAndDecodeUserId(encryptedEmail);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to extract email from token: " + ex.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Token validation failed: " + ex.getMessage());
            return false;
        }
    }
}

