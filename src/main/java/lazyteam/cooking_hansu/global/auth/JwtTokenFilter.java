package lazyteam.cooking_hansu.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JwtTokenFilter extends GenericFilter {

    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            String bearerToken = req.getHeader("Authorization");

            if (bearerToken == null) {
                chain.doFilter(request, response);
                return;
            }

            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            List<GrantedAuthority> authorityList = new ArrayList<>();
            authorityList.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role").toString()));
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorityList);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JWT 인증 성공: email={}, role={}", claims.getSubject(), claims.get("role"));
        } catch (Exception e) {
            log.error("JWT 파싱 실패", e);
        }
        chain.doFilter(request, response);
    }
}
