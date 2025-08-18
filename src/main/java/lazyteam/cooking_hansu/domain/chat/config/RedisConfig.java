package lazyteam.cooking_hansu.domain.chat.config;

import lazyteam.cooking_hansu.domain.chat.service.ChatRedisService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    //    연결기본객체
    @Bean
    @Qualifier("chatPubSub")
    public RedisConnectionFactory chatPubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
//        Redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음.
//        configuration.setDatabase(0); // 기본 데이터베이스 설정

        return new LettuceConnectionFactory(configuration);
    }

    //    publish 객체
    @Bean
    @Qualifier("chatPubSub")
//    일반적으로는 RedisTemplate<키데이터타입, 밸류데이터타입>을 사용
    public RedisTemplate<String, String> chatPubSubRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    //    subscribe 객체
    @Bean
    @Qualifier("chatMessageListenerContainer")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter)
    {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // 채팅 메시지와 읽음 상태 이벤트 모두 구독
        container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/chat-rooms/*/chat-message"));
        container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/chat-rooms/*/online-participant"));
        container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/chat-rooms/*/chat-participants"));
        return container;
    }

    //    redis에서 수신된 메시지를 처리하는 객체 생성
    @Bean
    public MessageListenerAdapter messageListenerAdapter(ChatRedisService redisPubSubService) {
//        RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

    @Bean
    @Qualifier("chatFactory")
    public RedisConnectionFactory chatRedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
        configuration.setDatabase(12);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("chatParticipants")
    public RedisTemplate<String, Map<String, String>> chatParticipantsRedisTemplate(
            @Qualifier("chatFactory") RedisConnectionFactory chatRedisConnectionFactory) {
        RedisTemplate<String, Map<String, String>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(chatRedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Map.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return redisTemplate;
    }

    @Bean
    @Qualifier("chatOnlineParticipants")
    public RedisTemplate<String, String> chatOnlineParticipantRedisTemplate(
            @Qualifier("chatFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    // 기본 redisTemplate 빈 추가 (필수)
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
