package com.example.app_be.core.security;

import com.example.app_be.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
//import lombok.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY;
    private final long EXPIRATION_SECOND;

    public JwtService(
            @Value("${app.security.jwt.secret}")
            String jwtSecret,
            @Value("${app.security.jwt.expiration_seconds}")
            long expiration
    ) {
        this.SECRET_KEY = jwtSecret;
        this.EXPIRATION_SECOND = expiration;
    }

    //
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Serialization
    public String generateToken(
            UserDetails userDetails,
            Map<String, Object> extraClaims
    ) {

        User user = (User) userDetails;

        return Jwts.builder()
                .claims(extraClaims)
//                .claim("role", user.getAuthorities())
                .claim("username", user.getUsername())
                .subject(user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + this.EXPIRATION_SECOND * 1000))
                .signWith(this.getSigningKey())
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null);
    }

    // Deserialization
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    UUID extractSubject(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Verification
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    boolean isTokenValid(String token, UserDetails userDetails) {
        User user = (User) userDetails;
        return (!isTokenExpired(token)) &&
                (extractSubject(token).equals(user.getId()));
    }
}
