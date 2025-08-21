package lazyteam.cooking_hansu.domain.interaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class ViewCountCacheService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    public ViewCountCacheService(@Qualifier("interactionStringRedisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ========== 게시글 좋아요 캐시 ==========

    public void addLikeCache(UUID postId, UUID userId) {
        String key = "post:" + postId + ":likes";
        redisTemplate.opsForSet().add(key, userId.toString());
        redisTemplate.expire(key, CACHE_TTL);
    }

    public void removeLikeCache(UUID postId, UUID userId) {
        String key = "post:" + postId + ":likes";
        redisTemplate.opsForSet().remove(key, userId.toString());
    }

    public Boolean isLikedFromCache(UUID postId, UUID userId) {
        try {
            String key = "post:" + postId + ":likes";
            return redisTemplate.opsForSet().isMember(key, userId.toString());
        } catch (Exception e) {
            log.warn("Redis 좋아요 캐시 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    public int getLikeCount(UUID postId) {
        try {
            String key = "post:" + postId + ":likes";
            Long count = redisTemplate.opsForSet().size(key);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.warn("Redis 좋아요 개수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    // ========== 게시글 북마크 캐시 ==========

    public void addBookmarkCache(UUID postId, UUID userId) {
        String key = "post:" + postId + ":bookmarks";
        redisTemplate.opsForSet().add(key, userId.toString());
        redisTemplate.expire(key, CACHE_TTL);
    }

    public void removeBookmarkCache(UUID postId, UUID userId) {
        String key = "post:" + postId + ":bookmarks";
        redisTemplate.opsForSet().remove(key, userId.toString());
    }

    public Boolean isBookmarkedFromCache(UUID postId, UUID userId) {
        try {
            String key = "post:" + postId + ":bookmarks";
            return redisTemplate.opsForSet().isMember(key, userId.toString());
        } catch (Exception e) {
            log.warn("Redis 북마크 캐시 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    public int getBookmarkCount(UUID postId) {
        try {
            String key = "post:" + postId + ":bookmarks";
            Long count = redisTemplate.opsForSet().size(key);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.warn("Redis 북마크 개수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    // ========== 강의 좋아요 캐시 ==========

    public void addLectureLikeCache(UUID lectureId, UUID userId) {
        String key = "lecture:" + lectureId + ":likes";
        redisTemplate.opsForSet().add(key, userId.toString());
        redisTemplate.expire(key, CACHE_TTL);
    }

    public void removeLectureLikeCache(UUID lectureId, UUID userId) {
        String key = "lecture:" + lectureId + ":likes";
        redisTemplate.opsForSet().remove(key, userId.toString());
    }

    public Boolean isLectureLikedFromCache(UUID lectureId, UUID userId) {
        try {
            String key = "lecture:" + lectureId + ":likes";
            return redisTemplate.opsForSet().isMember(key, userId.toString());
        } catch (Exception e) {
            log.warn("Redis 강의 좋아요 캐시 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    public int getLectureLikeCount(UUID lectureId) {
        try {
            String key = "lecture:" + lectureId + ":likes";
            Long count = redisTemplate.opsForSet().size(key);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.warn("Redis 강의 좋아요 개수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    // ========== 조회수 캐시 ==========

    public boolean hasViewed(UUID postId, UUID userId) {
        try {
            String key = "post:" + postId + ":viewers";
            return redisTemplate.opsForSet().isMember(key, userId.toString());
        } catch (Exception e) {
            log.warn("Redis 조회 기록 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    public void markAsViewed(UUID postId, UUID userId) {
        String key = "post:" + postId + ":viewers";
        redisTemplate.opsForSet().add(key, userId.toString());
        redisTemplate.expire(key, CACHE_TTL);
    }

    public int getViewCount(UUID postId) {
        try {
            String key = "post:" + postId + ":views";
            String count = redisTemplate.opsForValue().get(key);
            return count != null ? Integer.parseInt(count) : 0;
        } catch (Exception e) {
            log.warn("Redis 조회수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }
}
