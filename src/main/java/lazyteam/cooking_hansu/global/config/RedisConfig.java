package lazyteam.cooking_hansu.global.config;

import lazyteam.cooking_hansu.domain.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 통합 설정 클래스
 *
 * Redis Database 분리:
 * - DB 0: refresh token 저장소
 * - DB 1: 채팅 pub/sub
 * - DB 2: 상호작용 (좋아요, 북마크, 조회수 캐싱)
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    // ================================
    // DB 0: Refresh Token 저장소
    // ================================
    @Bean
    @Primary
    @Qualifier("rtInventory")
    public RedisConnectionFactory rtConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("rtInventory")
    public RedisTemplate<String, String> rtTemplate(@Qualifier("rtInventory") RedisConnectionFactory rtConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(rtConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    // ================================
    // DB 1: 채팅 Pub/Sub
    // ================================
    @Bean
    @Qualifier("chatPubSub")
    public RedisConnectionFactory chatPubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1); // 채팅용 DB 1번
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("chatPub")
    public StringRedisTemplate chatStringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

    // ================================
    // DB 2: 상호작용 (좋아요/북마크/조회수)
    // ================================
    @Bean
    @Qualifier("interactionRedis")
    public RedisConnectionFactory interactionRedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(2); // 상호작용용 DB 2번
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("interactionRedisTemplate")
    public RedisTemplate<String, Object> interactionRedisTemplate(@Qualifier("interactionRedis") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Qualifier("interactionStringRedisTemplate")
    public StringRedisTemplate interactionStringRedisTemplate(@Qualifier("interactionRedis") RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
