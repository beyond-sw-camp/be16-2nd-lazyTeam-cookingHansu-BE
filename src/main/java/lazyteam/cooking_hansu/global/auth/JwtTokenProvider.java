package lazyteam.cooking_hansu.global.auth;

import io.jsonwebtoken.Claims;
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
                            @Value("${jwt.secretKeyRt}") String secretKeyRt, @Value("${jwt.expirationRt}") int expirationRt) {
        this.secretKeyAt = secretKeyAt;
        this.expirationAt = expirationAt;
        this.secretKeyRt = secretKeyRt;
        this.expirationRt = expirationRt;
        this.secret_at_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyAt), SignatureAlgorithm.HS512
                .getJcaName());
        this.secret_rt_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
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

    // RT 유효 시간
    public long getRefreshTokenExpirationTime() {
        return expirationRt * 60 * 1000L; // milliseconds
    }

    // RT 유효성 검증
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret_rt_key)
                    .build()
                    .parseClaimsJws(refreshToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // AT 토큰에서 이메일 추출
    public String getEmailFromAccessToken(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret_at_key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // RT 토큰에서 이메일 추출
    public String getEmailFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret_rt_key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

}
