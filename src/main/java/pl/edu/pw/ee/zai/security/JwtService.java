package pl.edu.pw.ee.zai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.pw.ee.zai.repository.RevokedTokenRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;
    private final RevokedTokenRepository revokedTokenRepository;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds,
                      RevokedTokenRepository revokedTokenRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public Claims parseAndValidate(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtException("Token is empty");
        }
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String jti = claims.getId();
        if (jti != null && revokedTokenRepository.existsByTokenId(jti)) {
            throw new JwtException("Token has been revoked");
        }
        return claims;
    }

    public String extractTokenId(String token) {
        try {
            return parseAndValidate(token).getId();
        } catch (JwtException e) {
            return null;
        }
    }

    public Instant extractExpiration(String token) {
        try {
            Date exp = parseAndValidate(token).getExpiration();
            return exp != null ? exp.toInstant() : Instant.now().plusSeconds(expirationSeconds);
        } catch (JwtException e) {
            return Instant.now().plusSeconds(expirationSeconds);
        }
    }
}
