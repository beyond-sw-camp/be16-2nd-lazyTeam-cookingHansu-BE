package lazyteam.cooking_hansu.domain.interaction.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.interaction.dto.LectureLikeInfoDto;
import lazyteam.cooking_hansu.domain.interaction.entity.Bookmark;
import lazyteam.cooking_hansu.domain.interaction.entity.LectureLikes;
import lazyteam.cooking_hansu.domain.interaction.entity.Likes;
import lazyteam.cooking_hansu.domain.interaction.repository.BookmarkRepository;
import lazyteam.cooking_hansu.domain.interaction.repository.LectureLikesRepository;
import lazyteam.cooking_hansu.domain.interaction.repository.LikesRepository;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InteractionService {

    private final LikesRepository likesRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LectureLikesRepository lectureLikesRepository;
    private final PostRepository postRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    // 중복 방지를 위한 메모리 캐시
    private final Set<String> viewedPosts = new HashSet<>();

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    // ========== 게시글 좋아요 ==========

    public String togglePostLike(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));
        User user = getOrCreateDefaultUser(userId);

        Likes existingLike = likesRepository.findByUserIdAndPostId(userId, postId);

        if (existingLike != null) {
            // 좋아요 취소
            likesRepository.delete(existingLike);
            post.decrementLikeCount();
            log.info("좋아요 취소 - 게시글: {}, 사용자: {}, 현재 좋아요 수: {}", postId, userId, post.getLikeCount());
        } else {
            // 좋아요 추가
            Likes newLike = Likes.builder()
                    .user(user)
                    .post(post)
                    .build();
            likesRepository.save(newLike);
            post.incrementLikeCount();
            log.info("좋아요 추가 - 게시글: {}, 사용자: {}, 현재 좋아요 수: {}", postId, userId, post.getLikeCount());
        }
        
        postRepository.save(post);
        return existingLike != null ? "좋아요를 취소했습니다." : "좋아요를 추가했습니다.";
    }

    @Transactional(readOnly = true)
    public boolean isPostLiked(UUID postId, UUID userId) {
        return likesRepository.findByUserIdAndPostId(userId, postId) != null;
    }

    // ========== 게시글 북마크 ==========

    public String toggleBookmark(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));
        User user = getOrCreateDefaultUser(userId);

        Bookmark existingBookmark = bookmarkRepository.findByUserIdAndPostId(userId, postId);

        if (existingBookmark != null) {
            // 북마크 취소
            bookmarkRepository.delete(existingBookmark);
            post.decrementBookmarkCount();
            log.info("북마크 취소 - 게시글: {}, 사용자: {}, 현재 북마크 수: {}", postId, userId, post.getBookmarkCount());
        } else {
            // 북마크 추가
            Bookmark newBookmark = Bookmark.builder()
                    .user(user)
                    .post(post)
                    .build();
            bookmarkRepository.save(newBookmark);
            post.incrementBookmarkCount();
            log.info("북마크 추가 - 게시글: {}, 사용자: {}, 현재 북마크 수: {}", postId, userId, post.getBookmarkCount());
        }
        
        postRepository.save(post);
        return existingBookmark != null ? "북마크를 취소했습니다." : "북마크를 추가했습니다.";
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID postId, UUID userId) {
        return bookmarkRepository.findByUserIdAndPostId(userId, postId) != null;
    }

    // ========== 강의 좋아요 ==========

    public String toggleLectureLike(UUID lectureId, UUID userId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));
        User user = getOrCreateDefaultUser(userId);

        LectureLikes existingLike = lectureLikesRepository.findByUserIdAndLectureId(userId, lectureId);

        if (existingLike != null) {
            // 좋아요 취소
            lectureLikesRepository.delete(existingLike);
            log.info("강의 좋아요 취소 - 강의: {}, 사용자: {}", lectureId, userId);
            return "강의 좋아요를 취소했습니다.";
        } else {
            // 좋아요 추가
            LectureLikes newLike = LectureLikes.builder()
                    .user(user)
                    .lecture(lecture)
                    .build();
            lectureLikesRepository.save(newLike);
            log.info("강의 좋아요 추가 - 강의: {}, 사용자: {}", lectureId, userId);
            return "강의 좋아요를 추가했습니다.";
        }
    }

    @Transactional(readOnly = true)
    public boolean isLectureLiked(UUID lectureId, UUID userId) {
        return lectureLikesRepository.findByUserIdAndLectureId(userId, lectureId) != null;
    }

    @Transactional(readOnly = true)
    public LectureLikeInfoDto getLectureLikeInfo(UUID lectureId, UUID userId) {
        long likeCount = lectureLikesRepository.countByLectureId(lectureId);
        boolean isLiked = userId != null && isLectureLiked(lectureId, userId);

        return LectureLikeInfoDto.builder()
                .lectureId(lectureId)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }

    // ========== 조회수 ==========

    public void incrementViewCount(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));
        
        post.incrementViewCount();
        postRepository.save(post);
    }

    public boolean incrementViewCountWithCheck(UUID postId, UUID userId) {
        String viewKey = postId + ":" + userId;
        
        if (viewedPosts.contains(viewKey)) {
            return false; // 이미 조회한 경우
        }
        
        // 조회 기록 추가
        viewedPosts.add(viewKey);
        
        // 조회수 증가
        incrementViewCount(postId);
        
        return true; // 조회수 증가됨
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 사용자 조회 또는 기본 사용자 생성
     */
    private User getOrCreateDefaultUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseGet(() -> {
                    // 기본 사용자가 없으면 생성
                    User defaultUser = User.builder()
                            .name("기본사용자")
                            .email("default@test.com")
                            .nickname("기본사용자")
                            .password("password123")
                            .profileImageUrl("https://via.placeholder.com/150")
                            .build();
                    return userRepository.save(defaultUser);
                });
    }
}
