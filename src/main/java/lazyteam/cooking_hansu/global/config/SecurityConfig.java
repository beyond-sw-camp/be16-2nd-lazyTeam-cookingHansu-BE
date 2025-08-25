package lazyteam.cooking_hansu.global.config;

import lazyteam.cooking_hansu.global.auth.JwtAuthenticationHandler;
import lazyteam.cooking_hansu.global.auth.JwtAuthorizationHandler;
import lazyteam.cooking_hansu.global.auth.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationHandler jwtAuthenticationHandler;
    private final JwtAuthorizationHandler jwtAuthorizationHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("SecurityFilterChain 초기화됨 ✅");
        return httpSecurity
                .cors(cors -> cors.configurationSource(configurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e ->
                        e.authenticationEntryPoint(jwtAuthenticationHandler)
                                .accessDeniedHandler(jwtAuthorizationHandler)
                )
                // authorizeHttpRequests 내의 requestMatchers는 추후 수정할 예정
                .authorizeHttpRequests(a -> a.requestMatchers(
                        "/user/**", // Google OAuth 로그인
                        "/swagger-ui.html",
                        "/swagger-ui/**",// Swagger UI (html, js, css)
                        "/api-docs/**",       // OpenAPI JSON
                        "/v3/api-docs/**",       // OpenAPI JSON
                        "/swagger-resources/**", // Swagger 리소스
                        "/notice/**",
                        "/admin/**", // Admin 관련 API
                        "/report/**", // Report 관련 API
                        "/lecture/list", // 강의 목록조회 허용
                        "/lecture/detail/**", // 강의 상세 목록조회 허용
                        "/lecture/qna/*/list", // 강의 qna 조회 허용
                        "/review/list/**", // 강의 리뷰 조회 허용
                        "/user/**",
                        "/chat/**",
                        "/api/recipes/**",
                        "/api/posts/**", // 게시글 API 모두 허용
                        "/api/interactions/**", // 상호작용 API 모두 허용
                        "/connect/**", // WebSocket 연결 엔드포인트
                        "/topic/**", // WebSocket 토픽
                        "/publish/**", // WebSocket 메시지 발행
                        "/api/notifications/**", // 알림
                        "/api/my/**" // Mypage 관련 API
                ).permitAll().anyRequest().authenticated())
                .build();


    }

    private CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
