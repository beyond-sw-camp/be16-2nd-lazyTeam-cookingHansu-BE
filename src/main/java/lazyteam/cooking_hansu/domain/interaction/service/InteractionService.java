package lazyteam.cooking_hansu.domain.interaction.service;

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

    // ========== 게시글 좋아요 ==========

    public String togglePostLike(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        Likes existingLike = likesRepository.findByUserIdAndPostId(userId, postId);

        if (existingLike != null) {
            // 좋아요 취소
            likesRepository.delete(existingLike);
            post.decrementLikeCount();
            postRepository.save(post);
            return "좋아요를 취소했습니다.";
        } else {
            // 좋아요 추가
            Likes newLike = Likes.builder()
                    .user(user)
                    .post(post)
                    .build();
            likesRepository.save(newLike);
            post.incrementLikeCount();
            postRepository.save(post);
            return "좋아요를 추가했습니다.";
        }
    }

    @Transactional(readOnly = true)
    public boolean isPostLiked(UUID postId, UUID userId) {
        return likesRepository.findByUserIdAndPostId(userId, postId) != null;
    }

    // ========== 게시글 북마크 ==========

    public String toggleBookmark(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        Bookmark existingBookmark = bookmarkRepository.findByUserIdAndPostId(userId, postId);

        if (existingBookmark != null) {
            // 북마크 취소
            bookmarkRepository.delete(existingBookmark);
            post.decrementBookmarkCount();
            postRepository.save(post);
            return "북마크를 취소했습니다.";
        } else {
            // 북마크 추가
            Bookmark newBookmark = Bookmark.builder()
                    .user(user)
                    .post(post)
                    .build();
            bookmarkRepository.save(newBookmark);
            post.incrementBookmarkCount();
            postRepository.save(post);
            return "북마크를 추가했습니다.";
        }
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID postId, UUID userId) {
        return bookmarkRepository.findByUserIdAndPostId(userId, postId) != null;
    }

    // ========== 강의 좋아요 ==========

    public String toggleLectureLike(UUID lectureId, UUID userId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다. ID: " + lectureId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        LectureLikes existingLike = lectureLikesRepository.findByUserIdAndLectureId(userId, lectureId);

        if (existingLike != null) {
            // 좋아요 취소
            lectureLikesRepository.delete(existingLike);
            return "강의 좋아요를 취소했습니다.";
        } else {
            // 좋아요 추가
            LectureLikes newLike = LectureLikes.builder()
                    .user(user)
                    .lecture(lecture)
                    .build();
            lectureLikesRepository.save(newLike);
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
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + postId));
        
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
}
