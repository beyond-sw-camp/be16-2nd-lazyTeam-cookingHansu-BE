package lazyteam.cooking_hansu.domain.interaction.service;

import lazyteam.cooking_hansu.domain.interaction.dto.InteractionCountDto;
import lazyteam.cooking_hansu.domain.interaction.entity.Bookmark;
import lazyteam.cooking_hansu.domain.interaction.entity.Likes;
import lazyteam.cooking_hansu.domain.interaction.repository.BookmarkRepository;
import lazyteam.cooking_hansu.domain.interaction.repository.LikesRepository;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InteractionService {

    private final LikesRepository likesRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ViewCountCacheService viewCountCacheService;

    /**
     * 좋아요 추가/취소 토글 (Redis + DB 동기화)
     */
    public String toggleLike(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        Optional<Likes> existingLike = likesRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            likesRepository.delete(existingLike.get());
            post.decrementLikeCount();
            
            // Redis 캐시 업데이트
            updateLikeCacheAsync(postId, userId, false);
            
            log.info("좋아요 취소: 사용자 ID {}, 게시글 ID {}", userId, postId);
            return "좋아요가 취소되었습니다.";
        } else {
            // 좋아요 추가
            Likes like = Likes.builder()
                    .user(user)
                    .post(post)
                    .build();
            likesRepository.save(like);
            post.incrementLikeCount();
            
            // Redis 캐시 업데이트
            updateLikeCacheAsync(postId, userId, true);
            
            log.info("좋아요 추가: 사용자 ID {}, 게시글 ID {}", userId, postId);
            return "좋아요가 추가되었습니다.";
        }
    }

    /**
     * 북마크 추가/취소 토글 (Redis + DB 동기화)
     */
    public String toggleBookmark(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndPost(user, post);

        if (existingBookmark.isPresent()) {
            // 북마크 취소
            bookmarkRepository.delete(existingBookmark.get());
            post.decrementBookmarkCount();
            
            // Redis 캐시 업데이트
            updateBookmarkCacheAsync(postId, userId, false);
            
            log.info("북마크 취소: 사용자 ID {}, 게시글 ID {}", userId, postId);
            return "북마크가 취소되었습니다.";
        } else {
            // 북마크 추가
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .post(post)
                    .build();
            bookmarkRepository.save(bookmark);
            post.incrementBookmarkCount();
            
            // Redis 캐시 업데이트
            updateBookmarkCacheAsync(postId, userId, true);
            
            log.info("북마크 추가: 사용자 ID {}, 게시글 ID {}", userId, postId);
            return "북마크가 추가되었습니다.";
        }
    }

    /**
     * 좋아요 상태 확인 (DB 조회)
     */
    @Transactional(readOnly = true)
    public boolean isLiked(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        
        return likesRepository.existsByUserAndPost(user, post);
    }

    /**
     * 북마크 상태 확인 (DB 조회)
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        
        return bookmarkRepository.existsByUserAndPost(user, post);
    }

    /**
     * 게시글의 상호작용 카운트 조회 (Redis 우선, DB 백업)
     */
    @Transactional(readOnly = true)
    public InteractionCountDto getInteractionCounts(UUID postId) {
        Post post = findPostById(postId);
        
        // Redis에서 캐시된 카운트 조회 (Redis 우선)
        int likeCount = viewCountCacheService.getLikeCountFromCache(postId);
        int bookmarkCount = viewCountCacheService.getBookmarkCountFromCache(postId);
        int redisViewCount = viewCountCacheService.getViewCountFromCache(postId);
        
        // Redis에 데이터가 없으면 DB 값 사용
        if (likeCount == 0) {
            likeCount = post.getLikeCount();
        }
        if (bookmarkCount == 0) {
            bookmarkCount = post.getBookmarkCount();
        }
        int viewCount = redisViewCount > 0 ? redisViewCount : post.getViewCount();
        
        log.debug("카운트 조회 - Redis: like={}, bookmark={}, view={} | DB: like={}, bookmark={}, view={}", 
                viewCountCacheService.getLikeCountFromCache(postId),
                viewCountCacheService.getBookmarkCountFromCache(postId), 
                redisViewCount,
                post.getLikeCount(), 
                post.getBookmarkCount(), 
                post.getViewCount());
        
        return InteractionCountDto.builder()
                .likeCount(likeCount)
                .bookmarkCount(bookmarkCount)
                .viewCount(viewCount)
                .build();
    }

    /**
     * 조회수 증가 (기본)
     */
    public void incrementViewCount(UUID postId) {
        Post post = findPostById(postId);
        post.incrementViewCount();
        log.info("조회수 증가: 게시글 ID {}, 현재 조회수: {}", postId, post.getViewCount());
    }
    
    /**
     * 조회수 증가 (사용자 UUID 기반 중복 방지)
     */
    public boolean incrementViewCountWithDuplicateCheck(UUID postId, UUID userId) {
        // Redis에서 중복 체크 및 조회수 증가 (UUID만 사용)
        boolean incremented = viewCountCacheService.incrementViewCountInCache(postId, userId);
        
        if (incremented) {
            // 비동기로 DB 업데이트
            updateViewCountAsync(postId);
            log.info("조회수 증가: 게시글 ID {}, 사용자 ID {}", postId, userId);
        }
        
        return incremented;
    }
    
    /**
     * 비동기로 DB 조회수 업데이트
     */
    @Async
    public void updateViewCountAsync(UUID postId) {
        try {
            Post post = findPostById(postId);
            post.incrementViewCount();
            log.debug("DB 조회수 업데이트: 게시글 ID {}, 현재 조회수: {}", postId, post.getViewCount());
        } catch (Exception e) {
            log.error("DB 조회수 업데이트 실패: 게시글 ID {}", postId, e);
        }
    }
    
    /**
     * 비동기로 좋아요 캐시 업데이트
     */
    @Async
    public void updateLikeCacheAsync(UUID postId, UUID userId, boolean isLiked) {
        try {
            viewCountCacheService.updateLikeCache(postId, userId, isLiked);
            log.debug("Redis 좋아요 캐시 업데이트: postId={}, userId={}, isLiked={}", postId, userId, isLiked);
        } catch (Exception e) {
            log.error("Redis 좋아요 캐시 업데이트 실패: postId={}, userId={}", postId, userId, e);
        }
    }
    
    /**
     * 비동기로 북마크 캐시 업데이트
     */
    @Async
    public void updateBookmarkCacheAsync(UUID postId, UUID userId, boolean isBookmarked) {
        try {
            viewCountCacheService.updateBookmarkCache(postId, userId, isBookmarked);
            log.debug("Redis 북마크 캐시 업데이트: postId={}, userId={}, isBookmarked={}", postId, userId, isBookmarked);
        } catch (Exception e) {
            log.error("Redis 북마크 캐시 업데이트 실패: postId={}, userId={}", postId, userId, e);
        }
    }

    // ========== 헬퍼 메서드 ==========

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private Post findPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("게시글을 찾을 수 없습니다."));
    }
}
