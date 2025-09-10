//package lazyteam.cooking_hansu.domain.notification.config;
//
//import lazyteam.cooking_hansu.domain.notification.pubsub.NotificationPublisher;
//import lazyteam.cooking_hansu.domain.notification.pubsub.NotificationSubscriber;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisSseConfig {
//
//    // 알림용 RedisTemplate
//    @Bean(name = "ssePubSub")
//    public RedisTemplate<String, String> ssePubSubTemplate(
//            @Qualifier("chatPubSub") RedisConnectionFactory connectionFactory // 같은 Redis
//    ) {
//        RedisTemplate<String, String> t = new RedisTemplate<>();
//        t.setConnectionFactory(connectionFactory);
//        t.setKeySerializer(new StringRedisSerializer());
//        t.setValueSerializer(new StringRedisSerializer());
//        return t;
//    }
//
//    // 알림 전용 리스너 컨테이너
//    @Bean(name = "sseRedisMessageListenerContainer")
//    public RedisMessageListenerContainer sseRedisMessageListenerContainer(
//            @Qualifier("chatPubSub") RedisConnectionFactory connectionFactory,
//            NotificationSubscriber notificationSubscriber
//    ) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//
//
//        // 알림 채널 구독
//        container.addMessageListener(
//                notificationSubscriber,
//                new ChannelTopic(NotificationPublisher.CHANNEL)
//        );
//        return container;
//    }
//}