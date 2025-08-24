package lazyteam.cooking_hansu.domain.interaction.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.interaction.entity.Bookmark;
import lazyteam.cooking_hansu.domain.interaction.entity.LectureLikes;
import lazyteam.cooking_hansu.domain.interaction.entity.PostLikes;
import lazyteam.cooking_hansu.domain.interaction.repository.BookmarkRepository;
import lazyteam.cooking_hansu.domain.interaction.repository.LectureLikesRepository;
import lazyteam.cooking_hansu.domain.interaction.repository.PostLikesRepository;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InteractionService {

    private final PostLikesRepository postLikesRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LectureLikesRepository lectureLikesRepository;
    private final PostRepository postRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final RedisInteractionService redisInteractionService;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    // ========== 게시글 좋아요 ==========
    public String togglePostLike(UUID postId) {
        UUID userId = getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));
        User user = getCurrentUser();

        try {
            // Redis에서 좋아요 상태 확인 (먼저 Redis 체크)
            boolean isLiked = redisInteractionService.isPostLikedByUser(postId, userId);
            
            // Redis에 데이터가 없으면 DB에서 확인하고 Redis에 캐싱
            if (!isLiked) {
                boolean dbLikedStatus = postLikesRepository.findByUserIdAndPostId(userId, postId) != null;
                if (dbLikedStatus) {
                    redisInteractionService.setPostLikeStatus(postId, userId, true);
                    isLiked = true;
                }
            }

            if (isLiked) {
                // 좋아요 취소
                PostLikes existingLike = postLikesRepository.findByUserIdAndPostId(userId, postId);
                if (existingLike != null) {
                    postLikesRepository.delete(existingLike);
                }
                redisInteractionService.updatePostLike(postId, userId, false);

                // DB 카운트와 Redis 동기화
                Long actualCount = postLikesRepository.countByPostId(postId);
                redisInteractionService.setPostLikesCount(postId, actualCount);
                post.setLikeCount(actualCount);
                postRepository.save(post);

                log.info("좋아요 취소 - 게시글: {}, 사용자: {}, 총 개수: {}", postId, userId, actualCount);
                return "좋아요를 취소했습니다.";
            } else {
                // 좋아요 추가
                PostLikes newLike = PostLikes.builder().user(user).post(post).build();
                postLikesRepository.save(newLike);
                redisInteractionService.updatePostLike(postId, userId, true);

                // DB 카운트와 Redis 동기화
                Long actualCount = postLikesRepository.countByPostId(postId);
                redisInteractionService.setPostLikesCount(postId, actualCount);
                post.setLikeCount(actualCount);
                postRepository.save(post);

                log.info("좋아요 추가 - 게시글: {}, 사용자: {}, 총 개수: {}", postId, userId, actualCount);
                return "좋아요를 추가했습니다.";
            }
        } catch (Exception e) {
            log.error("좋아요 처리 실패 - postId: {}, userId: {}", postId, userId, e);
            // 오류 발생 시 Redis 캐시 무효화하여 다음 요청에서 DB에서 다시 읽어오도록 함
            redisInteractionService.deletePostLikesCache(postId);
            throw new RuntimeException("좋아요 처리 중 오류가 발생했습니다.", e);
        }
    }

    // ========== 게시글 북마크 ==========
    public String toggleBookmark(UUID postId) {
        UUID userId = getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));
        User user = getCurrentUser();

        try {
            // Redis에서 북마크 상태 확인 (먼저 Redis 체크)
            boolean isBookmarked = redisInteractionService.isPostBookmarkedByUser(postId, userId);

            // Redis에 데이터가 없으면 DB에서 확인하고 Redis에 캐싱
            if (!isBookmarked) {
                boolean dbBookmarkedStatus = bookmarkRepository.findByUserIdAndPostId(userId, postId) != null;
                if (dbBookmarkedStatus) {
                    redisInteractionService.setPostBookmarkStatus(postId, userId, true);
                    isBookmarked = true;
                }
            }

            if (isBookmarked) {
                // 북마크 취소
                Bookmark existingBookmark = bookmarkRepository.findByUserIdAndPostId(userId, postId);
                if (existingBookmark != null) {
                    bookmarkRepository.delete(existingBookmark);
                }
                redisInteractionService.updatePostBookmark(postId, userId, false);

                // DB 카운트와 Redis 동기화
                Long actualCount = bookmarkRepository.countByPostId(postId);
                redisInteractionService.setPostBookmarksCount(postId, actualCount);
                post.setBookmarkCount(actualCount);
                postRepository.save(post);

                log.info("북마크 취소 - 게시글: {}, 사용자: {}, 총 개수: {}", postId, userId, actualCount);
                return "북마크를 취소했습니다.";
            } else {
                // 북마크 추가
                Bookmark newBookmark = Bookmark.builder().user(user).post(post).build();
                bookmarkRepository.save(newBookmark);
                redisInteractionService.updatePostBookmark(postId, userId, true);

                // DB 카운트와 Redis 동기화
                Long actualCount = bookmarkRepository.countByPostId(postId);
                redisInteractionService.setPostBookmarksCount(postId, actualCount);
                post.setBookmarkCount(actualCount);
                postRepository.save(post);

                log.info("북마크 추가 - 게시글: {}, 사용자: {}, 총 개수: {}", postId, userId, actualCount);
                return "북마크를 추가했습니다.";
            }
        } catch (Exception e) {
            log.error("북마크 처리 실패 - postId: {}, userId: {}", postId, userId, e);
            // 오류 발생 시 Redis 캐시 무효화
            redisInteractionService.deletePostBookmarksCache(postId);
            throw new RuntimeException("북마크 처리 중 오류가 발생했습니다.", e);
        }
    }

    // ========== 강의 좋아요 ==========
    public String toggleLectureLike(UUID lectureId) {
        UUID userId = getCurrentUserId();
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));
        User user = getCurrentUser();

        try {
            // Redis에서 좋아요 상태 확인 (먼저 Redis 체크)
            boolean isLiked = redisInteractionService.isLectureLikedByUser(lectureId, userId);

            // Redis에 데이터가 없으면 DB에서 확인하고 Redis에 캐싱
            if (!isLiked) {
                boolean dbLikedStatus = lectureLikesRepository.findByUserIdAndLectureId(userId, lectureId) != null;
                if (dbLikedStatus) {
                    redisInteractionService.setLectureLikeStatus(lectureId, userId, true);
                    isLiked = true;
                }
            }

            if (isLiked) {
                // 좋아요 취소
                LectureLikes existingLike = lectureLikesRepository.findByUserIdAndLectureId(userId, lectureId);
                if (existingLike != null) {
                    lectureLikesRepository.delete(existingLike);
                }
                redisInteractionService.updateLectureLike(lectureId, userId, false);

                // DB 카운트와 Redis 동기화
                Long actualCount = lectureLikesRepository.countByLectureId(lectureId);
                redisInteractionService.setLectureLikesCount(lectureId, actualCount);

                log.info("강의 좋아요 취소 - lectureId: {}, userId: {}, 총 개수: {}", lectureId, userId, actualCount);
                return "강의 좋아요를 취소했습니다.";
            } else {
                // 좋아요 추가
                LectureLikes newLike = LectureLikes.builder().user(user).lecture(lecture).build();
                lectureLikesRepository.save(newLike);
                redisInteractionService.updateLectureLike(lectureId, userId, true);

                // DB 카운트와 Redis 동기화
                Long actualCount = lectureLikesRepository.countByLectureId(lectureId);
                redisInteractionService.setLectureLikesCount(lectureId, actualCount);

                log.info("강의 좋아요 추가 - lectureId: {}, userId: {}, 총 개수: {}", lectureId, userId, actualCount);
                return "강의 좋아요를 추가했습니다.";
            }
        } catch (Exception e) {
            log.error("강의 좋아요 처리 실패 - lectureId: {}, userId: {}", lectureId, userId, e);
            // 오류 발생 시 Redis 캐시 무효화
            redisInteractionService.deleteLectureLikesCache(lectureId);
            throw new RuntimeException("강의 좋아요 처리 중 오류가 발생했습니다.", e);
        }
    }

    // ========== 조회수 ==========
    public void incrementViewCount(UUID postId) {
        try {
            // Redis에서 조회수 관리
            Long cachedCount = redisInteractionService.getPostViewCount(postId);
            Long newViewCount;

            if (cachedCount == null) {
                // Redis에 캐시가 없으면 DB에서 현재 조회수를 가져와 Redis에 저장 후 증가
                Post post = postRepository.findById(postId).orElse(null);
                if (post != null) {
                    newViewCount = post.getViewCount() + 1;
                    redisInteractionService.setPostViewCount(postId, newViewCount);
                    post.setViewCount(newViewCount);
                    postRepository.save(post);
                } else {
                    throw new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId);
                }
            } else {
                // Redis에서 증가
                newViewCount = redisInteractionService.incrementPostViewCount(postId);

                // 주기적으로 DB 동기화 (10의 배수마다)
                if (newViewCount % 10 == 0) {
                    Post post = postRepository.findById(postId).orElse(null);
                    if (post != null) {
                        post.setViewCount(newViewCount);
                        postRepository.save(post);
                        log.debug("게시글 조회수 DB 동기화 완료 - postId: {}, count: {}", postId, newViewCount);
                    }
                }
            }

            log.debug("게시글 조회수 증가 - postId: {}, newCount: {}", postId, newViewCount);
        } catch (Exception e) {
            log.error("조회수 처리 실패 - postId: {}", postId, e);
            // 오류 발생 시 Redis 캐시 무효화
            redisInteractionService.deletePostViewCache(postId);
            throw new RuntimeException("조회수 처리 중 오류가 발생했습니다.", e);
        }
    }

    public boolean incrementViewCountWithCheck(UUID postId) {
        UUID userId = getCurrentUserId();

        // 이미 조회한 사용자인지 Redis에서 확인
        if (redisInteractionService.hasUserViewedPost(postId, userId)) {
            log.debug("이미 조회한 게시글 - postId: {}, userId: {}", postId, userId);
            return false;
        }

        // 조회 기록을 Redis에 저장하고 조회수 증가
        redisInteractionService.markPostAsViewed(postId, userId);
        incrementViewCount(postId);
        return true;
    }


    // ========== 유틸리티 메서드 ==========
    private UUID getCurrentUserId() {
        try {
            return UUID.fromString(testUserIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 UUID 형식: {}, 기본값 사용", testUserIdStr);
            // 기본 UUID 생성 (테스트용)
            return UUID.randomUUID();
        }
    }

    private User getCurrentUser() {
        UUID userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseGet(() -> {
                    log.info("사용자를 찾을 수 없어 기본 사용자 생성 - userId: {}", userId);
                    User testUser = User.builder()
                            .name("기본사용자")
                            .email("default@test.com")
                            .nickname("기본사용자")
                            .picture("https://via.placeholder.com/150")
                            .build();
                    return userRepository.save(testUser);
                });
    }
}