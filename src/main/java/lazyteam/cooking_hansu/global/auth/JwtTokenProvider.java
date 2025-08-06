package lazyteam.cooking_hansu.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expirationAt}")
    private int expirationAt;

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;

    private Key secret_rt_key;

    public JwtTokenProvider(@Value("${jwt.secretKeyAt}") String secretKeyAt, @Value("${jwt.expirationAt}") int expirationAt,
                            @Value("${jwt.secretKeyRt}") String serectKeyRt, @Value("${jwt.expirationRt}") int expirationRt) {
        this.secretKeyAt = secretKeyAt;
        this.expirationAt = expirationAt;
        this.secretKeyRt = serectKeyRt;
        this.expirationRt = expirationRt;
        this.secret_at_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyAt), SignatureAlgorithm.HS512
                .getJcaName());
        this.secret_rt_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(serectKeyRt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(User user) {
        String email = user.getEmail();
        String role = user.getRole().toString();

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationAt * 60 * 1000L))
                .signWith(secret_at_key)
                .compact();

        return token;
    }

    // RT 토큰 생성 메서드
    public String createRtToken(User user) {
        String email = user.getEmail();
        String role = user.getRole().toString();

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
                .signWith(secret_rt_key)
                .compact();

        return token;
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret_rt_key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Refresh Token에서 사용자 이메일 추출
    public String getEmailFromRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret_rt_key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // Refresh Token 만료 시간 반환 (초 단위)
    public long getRefreshTokenExpirationTime() {
        return expirationRt * 60L; // 분을 초로 변환
    }
}
