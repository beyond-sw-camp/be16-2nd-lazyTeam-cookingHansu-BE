package lazyteam.cooking_hansu.domain.chat.config;

import lazyteam.cooking_hansu.domain.chat.service.chatRedisService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

////    채팅 참여자 정보를 저장하는 RedisConnectionFactory
//    @Bean
//    @Qualifier("chatFactory")
//    public RedisConnectionFactory chatRedisConnectionFactory() {
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setHostName(redisHost);
//        configuration.setPort(redisPort);
//        configuration.setDatabase(12); // 채팅 참여자 정보를 저장할 데이터베이스 번호
//        return new LettuceConnectionFactory(configuration);
//    }


    //    publish 객체
    @Bean
    @Qualifier("chatPub")
//    일반적으로는 RedisTemplate<키데이터타입, 밸류데이터타입>을 사용
    public StringRedisTemplate stringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    //    subscribe 객체
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter)
    {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // 채팅 메시지와 읽음 상태 이벤트 모두 구독
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
        return container;
    }

    //    redis에서 수신된 메시지를 처리하는 객체 생성
    @Bean
    public MessageListenerAdapter messageListenerAdapter(chatRedisService redisPubSubService) {
//        RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }


//    //    RedisTemplate을 사용하여 채팅 참여자 정보를 저장하는 객체
//    @Bean
//    @Qualifier("chatTemplate")
//    public RedisTemplate<String, String> chatRedisTemplate(@Qualifier("chatFactory") RedisConnectionFactory ChatRedisConnectionFactory) {
//        RedisTemplate<String, String> redisTemplate = new StringRedisTemplate();
//        redisTemplate.setConnectionFactory(ChatRedisConnectionFactory);
//
//        // 모든 키와 값의 직렬화 방식을 StringRedisSerializer로 설정
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
//        return redisTemplate;
//    }
}
