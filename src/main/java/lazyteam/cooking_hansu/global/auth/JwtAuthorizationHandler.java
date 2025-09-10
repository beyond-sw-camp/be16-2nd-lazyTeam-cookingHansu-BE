package lazyteam.cooking_hansu.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@Slf4j
public class JwtAuthorizationHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("Error: " + accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ResponseDto<?> dto = ResponseDto.fail(HttpStatus.FORBIDDEN, "권한이 없습니다.");

        PrintWriter printWriter = response.getWriter();

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(dto);

        printWriter.write(body);
        printWriter.flush();
    }
}
