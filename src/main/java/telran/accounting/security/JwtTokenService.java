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

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private static final Duration JWT_TOKEN_VALIDITY = Duration.ofMinutes(240);
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
        userTokenCache.put(email, token);
        return token;
    }

    public String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token).getBody();
            String encryptedEmail = claims.getSubject();
            if (claims.getExpiration().before(new Date())) {
                return null;
            }
            return EmailEncryptionConfiguration.decryptAndDecodeUserId(encryptedEmail);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getCurrentProfileToken(String profileId){
        return this.userTokenCache.get(profileId);
    }
}

