package lazyteam.cooking_hansu.domain.interaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;


@Service
@Slf4j
public class RedisInteractionService {

    private final StringRedisTemplate redisTemplate;

    public RedisInteractionService(@Qualifier("interactionStringRedisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String LECTURE_LIKES_COUNT_KEY = "lecture:likes:count:%s";
    private static final String LECTURE_LIKES_USERS_KEY = "lecture:likes:users:%s";
    private static final String POST_LIKES_COUNT_KEY = "post:likes:count:%s";
    private static final String POST_LIKES_USERS_KEY = "post:likes:users:%s";
    private static final String POST_BOOKMARKS_COUNT_KEY = "post:bookmarks:count:%s";
    private static final String POST_BOOKMARKS_USERS_KEY = "post:bookmarks:users:%s";
    private static final String POST_VIEW_USERS_KEY = "post:views:users:%s";
    private static final String POST_VIEW_COUNT_KEY = "post:views:count:%s";

    // ========== TTL 설정 ==========
    private static final Duration INTERACTION_TTL = Duration.ofHours(24);    // 24시간
    private static final Duration POST_VIEWS_TTL = Duration.ofHours(2);      // 2시간 (조회수 중복 방지)

    // ========== 강의 좋아요 관련 ==========

    /**
     * 강의 좋아요 수 캐시에서 조회
     */
    public Long getLectureLikesCount(UUID lectureId) {
        String key = String.format(LECTURE_LIKES_COUNT_KEY, lectureId);
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : null;
    }

    /**
     * 강의 좋아요 수 캐시에 저장
     */
    public void setLectureLikesCount(UUID lectureId, Long count) {
        String key = String.format(LECTURE_LIKES_COUNT_KEY, lectureId);
        redisTemplate.opsForValue().set(key, count.toString(), INTERACTION_TTL);
        log.debug("강의 좋아요 수 캐시 저장 - lectureId: {}, count: {}", lectureId, count);
    }

    /**
     * 사용자가 특정 강의에 좋아요를 눌렀는지 확인
     */
    public boolean isLectureLikedByUser(UUID lectureId, UUID userId) {
        String key = String.format(LECTURE_LIKES_USERS_KEY, lectureId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 사용자의 강의 좋아요 상태 저장/삭제
     */
    public void setLectureLikeStatus(UUID lectureId, UUID userId, boolean isLiked) {
        String key = String.format(LECTURE_LIKES_USERS_KEY, lectureId);

        if (isLiked) {
            redisTemplate.opsForSet().add(key, userId.toString());
            log.debug("강의 좋아요 상태 추가 - lectureId: {}, userId: {}", lectureId, userId);
        } else {
            redisTemplate.opsForSet().remove(key, userId.toString());
            log.debug("강의 좋아요 상태 제거 - lectureId: {}, userId: {}", lectureId, userId);
        }

        // TTL 설정
        redisTemplate.expire(key, INTERACTION_TTL);
    }

    /**
     * 강의 좋아요 업데이트 (카운트 + 상태)
     */
    public void updateLectureLike(UUID lectureId, UUID userId, boolean isLiked) {
        // 사용자 좋아요 상태 업데이트
        setLectureLikeStatus(lectureId, userId, isLiked);

        // 좋아요 카운트 업데이트
        String countKey = String.format(LECTURE_LIKES_COUNT_KEY, lectureId);
        if (isLiked) {
            redisTemplate.opsForValue().increment(countKey);
        } else {
            Long count = redisTemplate.opsForValue().decrement(countKey);
            // 음수 방지
            if (count != null && count < 0) {
                redisTemplate.opsForValue().set(countKey, "0");
            }
        }
        // TTL 설정
        redisTemplate.expire(countKey, INTERACTION_TTL);

        log.info("강의 좋아요 {} - lectureId: {}, userId: {}",
                isLiked ? "추가" : "제거", lectureId, userId);
    }

    /**
     * 강의 좋아요 관련 캐시 삭제 (DB 동기화 시 사용)
     */
    public void deleteLectureLikesCache(UUID lectureId) {
        String countKey = String.format(LECTURE_LIKES_COUNT_KEY, lectureId);
        String usersKey = String.format(LECTURE_LIKES_USERS_KEY, lectureId);

        redisTemplate.delete(countKey);
        redisTemplate.delete(usersKey);

        log.info("강의 좋아요 캐시 삭제 - lectureId: {}", lectureId);
    }

    // ========== 게시글 좋아요 관련 ==========

    /**
     * 게시글 좋아요 수 캐시에 저장
     */
    public void setPostLikesCount(UUID postId, Long count) {
        String key = String.format(POST_LIKES_COUNT_KEY, postId);
        redisTemplate.opsForValue().set(key, count.toString(), INTERACTION_TTL);
        log.debug("게시글 좋아요 수 캐시 저장 - postId: {}, count: {}", postId, count);
    }

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     */
    public boolean isPostLikedByUser(UUID postId, UUID userId) {
        String key = String.format(POST_LIKES_USERS_KEY, postId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 사용자의 게시글 좋아요 상태 저장/삭제
     */
    public void setPostLikeStatus(UUID postId, UUID userId, boolean isLiked) {
        String key = String.format(POST_LIKES_USERS_KEY, postId);

        if (isLiked) {
            redisTemplate.opsForSet().add(key, userId.toString());
            log.debug("게시글 좋아요 상태 추가 - postId: {}, userId: {}", postId, userId);
        } else {
            redisTemplate.opsForSet().remove(key, userId.toString());
            log.debug("게시글 좋아요 상태 제거 - postId: {}, userId: {}", postId, userId);
        }

        // TTL 설정
        redisTemplate.expire(key, INTERACTION_TTL);
    }

    /**
     * 게시글 좋아요 업데이트 (카운트 + 상태)
     */
    public void updatePostLike(UUID postId, UUID userId, boolean isLiked) {
        // 사용자 좋아요 상태 업데이트
        setPostLikeStatus(postId, userId, isLiked);
        
        // 좋아요 카운트 업데이트
        String countKey = String.format(POST_LIKES_COUNT_KEY, postId);
        if (isLiked) {
            redisTemplate.opsForValue().increment(countKey);
        } else {
            Long count = redisTemplate.opsForValue().decrement(countKey);
            // 음수 방지
            if (count != null && count < 0) {
                redisTemplate.opsForValue().set(countKey, "0");
            }
        }
        // TTL 설정
        redisTemplate.expire(countKey, INTERACTION_TTL);
        
        log.info("게시글 좋아요 {} - postId: {}, userId: {}", 
                isLiked ? "추가" : "제거", postId, userId);
    }

    /**
     * 게시글 좋아요 관련 캐시 삭제
     */
    public void deletePostLikesCache(UUID postId) {
        String countKey = String.format(POST_LIKES_COUNT_KEY, postId);
        String usersKey = String.format(POST_LIKES_USERS_KEY, postId);

        redisTemplate.delete(countKey);
        redisTemplate.delete(usersKey);

        log.info("게시글 좋아요 캐시 삭제 - postId: {}", postId);
    }

    // ========== 게시글 북마크 관련 ==========

    /**
     * 게시글 북마크 수 캐시에 저장
     */
    public void setPostBookmarksCount(UUID postId, Long count) {
        String key = String.format(POST_BOOKMARKS_COUNT_KEY, postId);
        redisTemplate.opsForValue().set(key, count.toString(), INTERACTION_TTL);
        log.debug("게시글 북마크 수 캐시 저장 - postId: {}, count: {}", postId, count);
    }

    /**
     * 사용자가 특정 게시글에 북마크를 추가했는지 확인
     */
    public boolean isPostBookmarkedByUser(UUID postId, UUID userId) {
        String key = String.format(POST_BOOKMARKS_USERS_KEY, postId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 사용자의 게시글 북마크 상태 저장/삭제
     */
    public void setPostBookmarkStatus(UUID postId, UUID userId, boolean isBookmarked) {
        String key = String.format(POST_BOOKMARKS_USERS_KEY, postId);

        if (isBookmarked) {
            redisTemplate.opsForSet().add(key, userId.toString());
            log.debug("게시글 북마크 상태 추가 - postId: {}, userId: {}", postId, userId);
        } else {
            redisTemplate.opsForSet().remove(key, userId.toString());
            log.debug("게시글 북마크 상태 제거 - postId: {}, userId: {}", postId, userId);
        }

        // TTL 설정
        redisTemplate.expire(key, INTERACTION_TTL);
    }

    /**
     * 게시글 북마크 업데이트 (카운트 + 상태)
     */
    public void updatePostBookmark(UUID postId, UUID userId, boolean isBookmarked) {
        // 사용자 북마크 상태 업데이트
        setPostBookmarkStatus(postId, userId, isBookmarked);
        
        // 북마크 카운트 업데이트
        String countKey = String.format(POST_BOOKMARKS_COUNT_KEY, postId);
        if (isBookmarked) {
            redisTemplate.opsForValue().increment(countKey);
        } else {
            Long count = redisTemplate.opsForValue().decrement(countKey);
            // 음수 방지
            if (count != null && count < 0) {
                redisTemplate.opsForValue().set(countKey, "0");
            }
        }
        // TTL 설정
        redisTemplate.expire(countKey, INTERACTION_TTL);
        
        log.info("게시글 북마크 {} - postId: {}, userId: {}", 
                isBookmarked ? "추가" : "제거", postId, userId);
    }

    /**
     * 게시글 북마크 관련 캐시 삭제
     */
    public void deletePostBookmarksCache(UUID postId) {
        String countKey = String.format(POST_BOOKMARKS_COUNT_KEY, postId);
        String usersKey = String.format(POST_BOOKMARKS_USERS_KEY, postId);

        redisTemplate.delete(countKey);
        redisTemplate.delete(usersKey);

        log.info("게시글 북마크 캐시 삭제 - postId: {}", postId);
    }

    // ========== 게시글 조회수 관련 ==========

    /**
     * 사용자가 특정 게시글을 이미 조회했는지 확인
     */
    public boolean hasUserViewedPost(UUID postId, UUID userId) {
        String key = String.format(POST_VIEW_USERS_KEY, postId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 사용자의 게시글 조회 기록 저장 (중복 방지용)
     */
    public void markPostAsViewed(UUID postId, UUID userId) {
        String key = String.format(POST_VIEW_USERS_KEY, postId);
        redisTemplate.opsForSet().add(key, userId.toString());
        redisTemplate.expire(key, POST_VIEWS_TTL);

        log.debug("게시글 조회 기록 저장 - postId: {}, userId: {}", postId, userId);
    }

    /**
     * 게시글 조회수 캐시에서 조회
     */
    public Long getPostViewCount(UUID postId) {
        String key = String.format(POST_VIEW_COUNT_KEY, postId);
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : null;
    }

    /**
     * 게시글 조회수 캐시에 저장
     */
    public void setPostViewCount(UUID postId, Long count) {
        String key = String.format(POST_VIEW_COUNT_KEY, postId);
        redisTemplate.opsForValue().set(key, count.toString(), Duration.ofHours(1));
        log.debug("게시글 조회수 캐시 저장 - postId: {}, count: {}", postId, count);
    }

    /**
     * 게시글 조회수 증가
     */
    public Long incrementPostViewCount(UUID postId) {
        String key = String.format(POST_VIEW_COUNT_KEY, postId);
        Long newCount = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(1));

        log.info("게시글 조회수 증가 - postId: {}, newCount: {}", postId, newCount);
        return newCount;
    }

    /**
     * 게시글 조회 관련 캐시 삭제
     */
    public void deletePostViewCache(UUID postId) {
        String viewUsersKey = String.format(POST_VIEW_USERS_KEY, postId);
        String viewCountKey = String.format(POST_VIEW_COUNT_KEY, postId);

        redisTemplate.delete(viewUsersKey);
        redisTemplate.delete(viewCountKey);

        log.info("게시글 조회 캐시 삭제 - postId: {}", postId);
    }

}
