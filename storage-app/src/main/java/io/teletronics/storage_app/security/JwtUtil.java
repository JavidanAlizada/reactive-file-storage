package io.teletronics.storage_app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.teletronics.storage_app.exception.JwtTokenNotFoundOrInvalidException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Base64;
import java.util.Date;
import java.util.function.Function;


@Component
public class JwtUtil {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PART_OF_TOKEN = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = 7;
    private static final String USERNAME_CLAIM_KEY = "username";
    private final String secretKey;
    private final long validityInMilliseconds;

    public JwtUtil(@Value("${jwt.secret}") String secretKey,
                   @Value("${jwt.expires}") long validityInMilliseconds) {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String getTokenFromHeader(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PART_OF_TOKEN)) {
            return authHeader.substring(TOKEN_BEGIN_INDEX);
        }
        return null;
    }

    public String getUsernameFromToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PART_OF_TOKEN)) {
            String token = authHeader.substring(TOKEN_BEGIN_INDEX);
            return this.extractUsername(token);
        }
        throw new JwtTokenNotFoundOrInvalidException();
    }

    public String generateToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date validity = new Date(Long.sum(now.getTime(), validityInMilliseconds));

        claims.put(USERNAME_CLAIM_KEY, username);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Date getTokenExpiredDate() {
        return new Date(Long.sum(new Date().getTime(), validityInMilliseconds));
    }

    public boolean isValid(String token, UserDetails appUser) {
        String username = extractUsername(token);
        return username.equals(appUser.getUsername()) && !isTokenExpired(token);
    }

    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }
}
