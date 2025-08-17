package lazyteam.cooking_hansu.domain.interaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis를 활용한 상호작용 캐싱 서비스 (통합)
 * - 조회수 중복 방지
 * - 좋아요/북마크 캐싱 (선택적)
 * DB 2번 사용
 */
@Service
@Slf4j
public class ViewCountCacheService {

    private final StringRedisTemplate redisTemplate;
    
    // 생성자에서 @Qualifier 사용
    public ViewCountCacheService(@Qualifier("interactionStringRedisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis Key 패턴 (더 간결하게)
    private static final String VIEW_COUNT_KEY = "post:";        // post:{postId}:views
    private static final String VIEWED_USERS_KEY = "post:";      // post:{postId}:viewers  
    private static final String LIKE_COUNT_KEY = "post:";        // post:{postId}:like_count
    private static final String BOOKMARK_COUNT_KEY = "post:";    // post:{postId}:bookmark_count
    private static final String LIKE_USERS_KEY = "post:";        // post:{postId}:likers
    private static final String BOOKMARK_USERS_KEY = "post:";    // post:{postId}:bookmarkers
    
    private static final Duration EXPIRE_TIME = Duration.ofHours(24);

    /**
     * Redis에서 조회수 증가 (UUID 기반 중복 방지)
     */
    public boolean incrementViewCountInCache(UUID postId, UUID userId) {
        String viewedUsersKey = "post:" + postId + ":viewers";
        String viewCountKey = "post:" + postId + ":views";
        String userIdStr = userId.toString();

        // 이미 조회한 사용자인지 확인
        if (redisTemplate.opsForSet().isMember(viewedUsersKey, userIdStr)) {
            log.debug("이미 조회한 사용자: postId={}, userId={}", postId, userId);
            return false;
        }

        // 조회 기록 추가
        redisTemplate.opsForSet().add(viewedUsersKey, userIdStr);
        redisTemplate.expire(viewedUsersKey, EXPIRE_TIME);

        // Redis에서 조회수 증가
        redisTemplate.opsForValue().increment(viewCountKey);
        redisTemplate.expire(viewCountKey, EXPIRE_TIME);

        log.info("Redis 조회수 증가: postId={}, userId={}", postId, userId);
        return true;
    }

    /**
     * Redis에서 조회수 조회
     */
    public int getViewCountFromCache(UUID postId) {
        String key = "post:" + postId + ":views";
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * Redis 캐시 초기화 (테스트용)
     */
    public void clearViewCache(UUID postId) {
        String viewedUsersKey = "post:" + postId + ":viewers";
        String viewCountKey = "post:" + postId + ":views";
        redisTemplate.delete(viewedUsersKey);
        redisTemplate.delete(viewCountKey);
        log.info("Redis 조회 캐시 초기화: postId={}", postId);
    }


    
    // ========== 좋아요/북마크 캐싱 ==========
    
    /**
     * 좋아요 캐시 업데이트
     */
    public void updateLikeCache(UUID postId, UUID userId, boolean isLiked) {
        String likeUsersKey = "post:" + postId + ":likers";
        String userIdStr = userId.toString();
        
        if (isLiked) {
            redisTemplate.opsForSet().add(likeUsersKey, userIdStr);
        } else {
            redisTemplate.opsForSet().remove(likeUsersKey, userIdStr);
        }
        redisTemplate.expire(likeUsersKey, EXPIRE_TIME);
        
        log.debug("좋아요 캐시 업데이트: postId={}, userId={}, isLiked={}", postId, userId, isLiked);
    }
    
    /**
     * 북마크 캐시 업데이트
     */
    public void updateBookmarkCache(UUID postId, UUID userId, boolean isBookmarked) {
        String bookmarkUsersKey = "post:" + postId + ":bookmarkers";
        String userIdStr = userId.toString();
        
        if (isBookmarked) {
            redisTemplate.opsForSet().add(bookmarkUsersKey, userIdStr);
        } else {
            redisTemplate.opsForSet().remove(bookmarkUsersKey, userIdStr);
        }
        redisTemplate.expire(bookmarkUsersKey, EXPIRE_TIME);
        
        log.debug("북마크 캐시 업데이트: postId={}, userId={}, isBookmarked={}", postId, userId, isBookmarked);
    }
    
    /**
     * 좋아요 상태 캐시 조회
     */
    public boolean isLikedFromCache(UUID postId, UUID userId) {
        String likeUsersKey = "post:" + postId + ":likers";
        Boolean result = redisTemplate.opsForSet().isMember(likeUsersKey, userId.toString());
        return result != null && result;
    }
    
    /**
     * 북마크 상태 캐시 조회
     */
    public boolean isBookmarkedFromCache(UUID postId, UUID userId) {
        String bookmarkUsersKey = "post:" + postId + ":bookmarkers";
        Boolean result = redisTemplate.opsForSet().isMember(bookmarkUsersKey, userId.toString());
        return result != null && result;
    }
    
    /**
     * Redis에서 좋아요 카운트 조회
     */
    public int getLikeCountFromCache(UUID postId) {
        String likeUsersKey = "post:" + postId + ":likers";
        Long count = redisTemplate.opsForSet().size(likeUsersKey);
        return count != null ? count.intValue() : 0;
    }
    
    /**
     * Redis에서 북마크 카운트 조회
     */
    public int getBookmarkCountFromCache(UUID postId) {
        String bookmarkUsersKey = "post:" + postId + ":bookmarkers";
        Long count = redisTemplate.opsForSet().size(bookmarkUsersKey);
        return count != null ? count.intValue() : 0;
    }
}
