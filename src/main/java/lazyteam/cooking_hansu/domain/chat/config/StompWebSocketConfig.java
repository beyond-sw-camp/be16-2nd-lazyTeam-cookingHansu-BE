package lazyteam.cooking_hansu.domain.chat.config;

import lazyteam.cooking_hansu.domain.chat.handler.StompHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
// EnableWebSocketMessageBroker 어노테이션은 STOMP 프로토콜을 사용하여 메시지 브로커를 활성화
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    public StompWebSocketConfig(StompHandler stompHandler) {
        this.stompHandler = stompHandler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOrigins("http://localhost:3000", "https://www.cookinghansu.shop") // CORS 설정
//                ws://가 아닌 http:// 엔드포인트를 사용할 수 있게 해주는 sockJs라이브러리를 통한 요청을 허용하는 설정.
                .withSockJS(); // SockJS를 사용하여 WebSocket 연결을 지원
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        /publish/1 와 같은 경로로 메시지를 발행할 수 있도록 설정 --> 이 경로대로 메시지 발행해야 함을 설정
//        /publish로 시작하는 url패턴으로 메시지가 발행되면 @Controller 객체의 @MessageMapping 어노테이션이 붙은 메소드로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");
//        /topic/1 와 같은 경로로 메시지를 구독할 수 있도록 설정 --> 이 경로대로 메시지 수신해야 함을 설정
        registry.enableSimpleBroker("/topic");
    }

//    웹소켓요청(connect, subscribe, disconnect 등)의 요청시에는 http header등 http메시지를 넣어올 수 있고
//    이를 interceptor를 통해 가로채 토큰등을 검증할 수 있음.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
