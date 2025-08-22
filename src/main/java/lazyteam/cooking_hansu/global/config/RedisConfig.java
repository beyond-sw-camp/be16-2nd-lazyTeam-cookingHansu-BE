package lazyteam.cooking_hansu.global.config;

import lazyteam.cooking_hansu.domain.chat.service.RedisPubSubService;
import lazyteam.cooking_hansu.domain.notification.pubsub.NotificationPublisher;
import lazyteam.cooking_hansu.domain.notification.pubsub.NotificationSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 환경 설정 클래스
 * redis database 목록
 * 0: refresh token 저장소
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    @Qualifier("rtTemplate")
    public RedisConnectionFactory rtConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("rtTemplate")
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtTemplate") RedisConnectionFactory rtConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(rtConnectionFactory);
        return redisTemplate;
    }

    //    연결기본객체
    @Bean
    @Qualifier("chatPubSub")
    public RedisConnectionFactory chatPubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
//        Redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음.
        configuration.setDatabase(1); // 기본 데이터베이스 설정

        return new LettuceConnectionFactory(configuration);
    }

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
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
        return container;
    }

    //    redis에서 수신된 메시지를 처리하는 객체 생성
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
//        RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

    // 알림용 RedisTemplate
    @Bean(name = "ssePubSub")
    public RedisTemplate<String, String> ssePubSubTemplate(
            @Qualifier("chatPubSub") RedisConnectionFactory connectionFactory // 같은 Redis
    ) {
        RedisTemplate<String, String> t = new RedisTemplate<>();
        t.setConnectionFactory(connectionFactory);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new StringRedisSerializer());
        return t;
    }

    // 알림 전용 리스너 컨테이너
    @Bean(name = "sseRedisMessageListenerContainer")
    public RedisMessageListenerContainer sseRedisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory connectionFactory,
            NotificationSubscriber notificationSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);


        // 알림 채널 구독
        container.addMessageListener(
                notificationSubscriber,
                new ChannelTopic(NotificationPublisher.CHANNEL)
        );
        return container;
    }
}
