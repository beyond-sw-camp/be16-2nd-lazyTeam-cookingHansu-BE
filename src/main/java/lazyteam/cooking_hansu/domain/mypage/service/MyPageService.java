package lazyteam.cooking_hansu.domain.mypage.service;


import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.interaction.entity.*;
import lazyteam.cooking_hansu.domain.interaction.repository.*;
import lazyteam.cooking_hansu.domain.lecture.entity.*;
import lazyteam.cooking_hansu.domain.lecture.repository.*;
import lazyteam.cooking_hansu.domain.mypage.dto.*;
import lazyteam.cooking_hansu.domain.post.entity.*;
import lazyteam.cooking_hansu.domain.post.repository.*;
import lazyteam.cooking_hansu.domain.purchase.entity.*;
import lazyteam.cooking_hansu.domain.purchase.repository.*;
import lazyteam.cooking_hansu.domain.user.entity.common.*;
import lazyteam.cooking_hansu.domain.user.repository.*;
import lazyteam.cooking_hansu.global.service.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.multipart.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PurchasedLectureRepository purchasedLectureRepository;
    private final LectureReviewRepository lectureReviewRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostLikesRepository postLikesRepository;
    private final S3Uploader s3Uploader;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    // ===== 프로필 관련 메서드 =====

    // 프로필 조회
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile() {
        User user = getCurrentUser();

        return ProfileResponseDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .info(user.getInfo())
                .profileImageUrl(user.getProfileImageUrl())
                .userType(getUserTypeDisplayName(user.getRole()))
                .build();
    }

    // 프로필 수정
    public ProfileResponseDto updateProfile(ProfileUpdateRequestDto requestDto) {
        User user = getCurrentUser();

        // 닉네임 중복 검사 (자신의 닉네임이 아닌 경우)
        if (!user.getNickname().equals(requestDto.getNickname()) &&
                userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 프로필 정보 업데이트
        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getInfo(),
                requestDto.getProfileImageUrl()
        );

        return getProfile();
    }

    // 프로필 이미지 업로드
    public String uploadProfileImage(MultipartFile image) {
        System.out.println("=== 이미지 업로드 시작 ===");
        System.out.println("파일명: " + image.getOriginalFilename());
        System.out.println("파일 크기: " + image.getSize() + " bytes");

        User user = getCurrentUser();

        // 기존 이미지가 있다면 S3에서 삭제
        if (user.getProfileImageUrl() != null) {
            try {
                s3Uploader.delete(user.getProfileImageUrl());
                System.out.println("기존 이미지 삭제 완료");
            } catch (Exception e) {
                System.out.println("기존 이미지 삭제 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 새 이미지 업로드
        System.out.println("새 이미지 S3 업로드 시작");
        try {
            String imageUrl = s3Uploader.upload(image, "profile-images/");
            System.out.println("S3 업로드 완료: " + imageUrl);

            // 사용자 프로필 이미지 URL 업데이트
            user.updateProfileImage(imageUrl);
            System.out.println("사용자 프로필 이미지 업데이트 완료");
            return imageUrl;
        } catch (Exception e) {
            System.out.println("S3 업로드 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    // ===== 내 게시글 관련 메서드 =====

    @Transactional(readOnly = true)
    public List<MyPostListDto> getMyPosts() {
        User user = getCurrentUser();
        List<Post> posts = postRepository.findAllByUser(user);

        return posts.stream()
                .map(post -> MyPostListDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .description(post.getDescription())
                        .thumbnailUrl(post.getThumbnailUrl())
                        .createdAt(post.getCreatedAt())
                        .likeCount(post.getLikeCount())
                        .bookmarkCount(post.getBookmarkCount())
                        .build())
                .collect(Collectors.toList());
    }

    // ===== 구매한 강의 관련 메서드 =====

    @Transactional(readOnly = true)
    public Page<MyLectureListDto> getMyLectures(Pageable pageable) {
        User user = getCurrentUser();

        // 해당 유저가 구매한 강의 목록
        Page<PurchasedLecture> purchases = purchasedLectureRepository.findAllByUser(user, pageable);

        return purchases.map(purchase -> {
            Lecture lecture = purchase.getLecture();

            // 평균 평점 계산
            List<LectureReview> reviews = lectureReviewRepository.findAllByLectureId(lecture.getId());
            double avgRating = reviews.isEmpty() ? 0.0 :
                    reviews.stream().mapToInt(LectureReview::getRating).average().orElse(0.0);

            // 수강생 수 계산
            int studentCount = purchasedLectureRepository.countByLecture(lecture);

            return MyLectureListDto.builder()
                    .id(lecture.getId())
                    .category(lecture.getCategory().toString())
                    .title(lecture.getTitle())
                    .description(lecture.getDescription())
                    .averageRating(avgRating)
                    .studentCount(studentCount)
                    .thumbnailUrl(lecture.getThumbUrl())
                    .build();
        });
    }

    // ===== 북마크 관련 메서드 =====

    @Transactional(readOnly = true)
    public List<MyBookmarkLikedListDto> getMyBookmarks() {
        User user = getCurrentUser();

        return bookmarkRepository.findAllByUser(user).stream()
                .map(bookmark -> {
                    Post post = bookmark.getPost();
                    return MyBookmarkLikedListDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .description(post.getDescription())
                            .thumbnailUrl(post.getThumbnailUrl())
                            .likeCount(post.getLikeCount())
                            .bookmarkCount(post.getBookmarkCount())
                            .writerNickname(post.getUser().getNickname())
                            .createdAt(post.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===== 좋아요 관련 메서드 =====

    @Transactional(readOnly = true)
    public List<MyBookmarkLikedListDto> getMyLikes() {
        User user = getCurrentUser();
        List<Likes> likesList = postLikesRepository.findAllByUser(user);

        return likesList.stream()
                .map(like -> {
                    Post post = like.getPost();
                    return MyBookmarkLikedListDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .description(post.getDescription())
                            .thumbnailUrl(post.getThumbnailUrl())
                            .likeCount(post.getLikeCount())
                            .bookmarkCount(post.getBookmarkCount())
                            .writerNickname(post.getUser().getNickname())
                            .createdAt(post.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===== 공통 메서드 =====

    // 현재 로그인한 사용자 조회 (테스트용)
    private User getCurrentUser() {
        UUID testUserId = UUID.fromString(testUserIdStr);
        return userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 역할명을 한글로 변환하는 메서드
    private String getUserTypeDisplayName(Role role) {
        switch (role) {
            case GENERAL:
                return "일반 사용자";
            case CHEF:
                return "요리사";
            case OWNER:
                return "사업자";
            default:
                return "사용자";
        }
    }
}