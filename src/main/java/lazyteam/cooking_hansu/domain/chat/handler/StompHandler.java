package lazyteam.cooking_hansu.domain.chat.handler;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lazyteam.cooking_hansu.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    private final ChatService chatService;

    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 테스팅을 위해 토큰 검증 주석처리
        if(StompCommand.CONNECT == accessor.getCommand()){
            System.out.println("connect요청시 토큰 유효성 검증 (테스팅 모드 - 검증 생략)");
            // String bearerToken = accessor.getFirstNativeHeader("Authorization");
            // String token = bearerToken.substring(7);
            // Jwts.parserBuilder()
            //         .setSigningKey(secretKey)
            //         .build()
            //         .parseClaimsJws(token)
            //         .getBody();
            System.out.println("토큰 유효성 검증 성공 (테스팅 모드)");
        }
        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            System.out.println("Subscribe 검증 (테스팅 모드 - 검증 생략)");
            // String bearerToken = accessor.getFirstNativeHeader("Authorization");
            // String token = bearerToken.substring(7);
            // Claims claims = Jwts.parserBuilder()
            //         .setSigningKey(secretKey)
            //         .build()
            //         .parseClaimsJws(token)
            //         .getBody();
            // UUID userId = UUID.fromString(claims.getSubject());
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            UUID roomId = UUID.fromString(accessor.getDestination().split("/")[2]); // destination은 /topic/{roomId} 형태로 오기 때문에 split을 통해 roomId 추출
            if(!chatService.isRoomParticipant(userId, roomId)){
                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
            }
        }
        return message;
    }
}
