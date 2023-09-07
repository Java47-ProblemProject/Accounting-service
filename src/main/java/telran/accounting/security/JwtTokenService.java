package telran.accounting.security;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import telran.accounting.configuration.EmailEncryptionConfiguration;
import telran.accounting.model.Profile;
import telran.accounting.model.Roles;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private static final Duration JWT_TOKEN_VALIDITY = Duration.ofHours(6);
    @Value("${jwt.secret-key}")
    private String jwtSecretKey;
    private SecretKey jwtSecret;
    private final Map<String, String> userTokenCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        byte[] secretBytes = jwtSecretKey.getBytes();
        jwtSecret = new SecretKeySpec(secretBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public void generateToken(Profile profile) {
        Instant now = Instant.now();
        String email = profile.getEmail();
        if (userTokenCache.containsKey(email)) {
            String existingToken = userTokenCache.get(email);
            return;
        }
        Set<String> roleStrings = profile.getRoles().stream()
                .map(Roles::name)
                .collect(Collectors.toSet());
        String token = Jwts.builder()
                .setSubject(email)
                .claim("roles", roleStrings)
                .setIssuer("app")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(JWT_TOKEN_VALIDITY)))
                .signWith(jwtSecret)
                .compact();
        userTokenCache.put(email, token);
    }

    public String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token).getBody();
            String encryptedEmail = claims.getSubject();
            if (claims.getExpiration().before(new Date())) {
                return "";
            }
            return EmailEncryptionConfiguration.decryptAndDecodeUserId(encryptedEmail);
        } catch (Exception ex) {
            return "";
        }
    }

    public Set<String> extractRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token).getBody();
            String[] roles = claims.get("roles").toString().replace("[", "").replace("]", "").trim().split(",");
            return new HashSet<>(Arrays.asList(roles));
        } catch (Exception ex) {
            return Collections.emptySet();
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token).getBody();
            String email = claims.getSubject();
            String cachedToken = userTokenCache.get(email);
            // Если токен не найден в кеше или не совпадает с переданным токеном, он считается недействительным
            if (cachedToken == null || !cachedToken.equals(token)) {
                return false;
            }
            // Дополнительно проверяем срок действия токена
            return !claims.getExpiration().before(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public String getCurrentProfileToken(String profileId) {
        return this.userTokenCache.get(profileId);
    }

    public void deleteCurrentProfileToken(String profileId) {
        this.userTokenCache.remove(profileId);
    }
}

