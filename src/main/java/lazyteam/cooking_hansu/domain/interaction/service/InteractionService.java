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
import lazyteam.cooking_hansu.global.exception.CustomException;
import lazyteam.cooking_hansu.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 좋아요 추가/취소 토글
     */
    public String toggleLike(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        Optional<Likes> existingLike = likesRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            likesRepository.delete(existingLike.get());
            post.decrementLikeCount();
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
            log.info("좋아요 추가: 사용자 ID {}, 게시글 ID {}", userId, postId);
            return "좋아요가 추가되었습니다.";
        }
    }

    /**
     * 북마크 추가/취소 토글
     */
    public String toggleBookmark(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndPost(user, post);

        if (existingBookmark.isPresent()) {
            // 북마크 취소
            bookmarkRepository.delete(existingBookmark.get());
            post.decrementBookmarkCount();
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
            log.info("북마크 추가: 사용자 ID {}, 게시글 ID {}", userId, postId);
            return "북마크가 추가되었습니다.";
        }
    }

    /**
     * 좋아요 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isLiked(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        
        return likesRepository.existsByUserAndPost(user, post);
    }

    /**
     * 북마크 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID postId, UUID userId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        
        return bookmarkRepository.existsByUserAndPost(user, post);
    }

    /**
     * 게시글의 상호작용 카운트 조회
     */
    @Transactional(readOnly = true)
    public InteractionCountDto getInteractionCounts(UUID postId) {
        Post post = findPostById(postId);
        
        return InteractionCountDto.builder()
                .likeCount(post.getLikeCount())
                .bookmarkCount(post.getBookmarkCount())
                .viewCount(post.getViewCount())
                .build();
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount(UUID postId) {
        Post post = findPostById(postId);
        post.incrementViewCount();
        log.info("조회수 증가: 게시글 ID {}, 현재 조회수: {}", postId, post.getViewCount());
    }

    // ========== 헬퍼 메서드 ==========

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
