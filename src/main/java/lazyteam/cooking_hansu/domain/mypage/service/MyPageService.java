package lazyteam.cooking_hansu.domain.mypage.service;


import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.comment.repository.PostCommentRepository;
import lazyteam.cooking_hansu.domain.interaction.entity.*;
import lazyteam.cooking_hansu.domain.interaction.repository.*;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.mypage.dto.*;
import lazyteam.cooking_hansu.domain.post.entity.*;
import lazyteam.cooking_hansu.domain.post.repository.*;
import lazyteam.cooking_hansu.domain.purchase.repository.*;
import lazyteam.cooking_hansu.domain.user.entity.common.*;
import lazyteam.cooking_hansu.domain.user.repository.*;
import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lazyteam.cooking_hansu.global.service.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MyPageService.class);
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PurchasedLectureRepository purchasedLectureRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostLikesRepository postLikesRepository;
    private final S3Uploader s3Uploader;
    private final PostCommentRepository postCommentRepository;
    private final LectureLikesRepository lectureLikesRepository;

    // ===== 프로필 관련 메서드 =====

    // 프로필 조회
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile() {
        User user = getCurrentUser();

        return ProfileResponseDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .info(user.getInfo())
                .profileImageUrl(user.getPicture())
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
    @Transactional
    public String uploadProfileImage(MultipartFile image) {
        System.out.println("=== 이미지 업로드 시작 ===");
        System.out.println("파일명: " + image.getOriginalFilename());
        System.out.println("파일 크기: " + image.getSize() + " bytes");

        User user = getCurrentUser();

        // 기존 이미지가 있다면 S3에서 삭제
        if (user.getPicture() != null) {
            try {
                s3Uploader.delete(user.getPicture());
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
            // 즉시 DB 반영
            userRepository.save(user);
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
        List<Post> posts = postRepository.findAllByUserAndDeletedAtIsNull(user);

        return posts.stream()
                .map(post -> { Long commentCount = postCommentRepository.countByPostAndCommentIsDeletedFalse(post);
                        return MyPostListDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .description(post.getDescription())
                        .thumbnailUrl(post.getThumbnailUrl())
                        .createdAt(post.getCreatedAt())
                        .likeCount(post.getLikeCount())
                        .category(post.getCategory())
                        .bookmarkCount(post.getBookmarkCount())
                        .isOpen(post.getIsOpen())
                        .commentCount(commentCount)
                        .build();
                })
                .collect(Collectors.toList());
    }

    // ====== 내 강의 목록 조회 ======

    public Page<MyLectureListDto> getMyLectures(Pageable pageable) {
        UUID userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        Page<MyLectureListDto> dtos = purchasedLectureRepository.findAllByUser(user,pageable)
                .map(MyLectureListDto::fromEntity);
        log.info("내가 구매한 강의 목록 : " + dtos.toString());
        return dtos;
    }


    // ===== 북마크 관련 메서드 =====

    @Transactional(readOnly = true)
    public List<MyBookmarkLikedListDto> getMyBookmarks() {
        User user = getCurrentUser();

        return bookmarkRepository.findAllByUser(user).stream()
                .filter(bookmark -> {
                    Post post = bookmark.getPost();
                    // 내 게시글이거나 공개 게시글만 보이도록 필터링
                    return post.getIsOpen() || post.getUser().getId().equals(user.getId());
                })
                .map(bookmark -> {
                    Post post = bookmark.getPost();
                    Long commentCount = postCommentRepository.countByPostAndCommentIsDeletedFalse(post);

                    return MyBookmarkLikedListDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .description(post.getDescription())
                            .thumbnailUrl(post.getThumbnailUrl())
                            .likeCount(post.getLikeCount())
                            .category(post.getCategory())
                            .bookmarkCount(post.getBookmarkCount())
                            .writerNickname(post.getUser().getNickname())
                            .createdAt(post.getCreatedAt())
                            .commentCount(commentCount)
                            .isOpen(post.getIsOpen())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===== 레시피 좋아요 관련 메서드 =====

    @Transactional(readOnly = true)
    public List<MyBookmarkLikedListDto> getMyLikes() {
        User user = getCurrentUser();
        List<PostLikes> postLikesList = postLikesRepository.findAllByUser(user);

        return postLikesList.stream()
                .filter(like -> {
                    Post post = like.getPost();
                    // 내 게시글이거나 공개 게시글만 보이도록 필터링
                    return post.getIsOpen() || post.getUser().getId().equals(user.getId());
                })
                .map(like -> {
                    Post post = like.getPost();
                    Long commentCount = postCommentRepository.countByPostAndCommentIsDeletedFalse(post);

                    return MyBookmarkLikedListDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .description(post.getDescription())
                            .thumbnailUrl(post.getThumbnailUrl())
                            .category(post.getCategory())
                            .likeCount(post.getLikeCount())
                            .bookmarkCount(post.getBookmarkCount())
                            .writerNickname(post.getUser().getNickname())
                            .createdAt(post.getCreatedAt())
                            .commentCount(commentCount)
                            .isOpen(post.getIsOpen())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===== 강의 좋아요 관련 메서드 =====


    @Transactional(readOnly = true)
    public List<MyLectureListDto> getMyLikedLectures() {
        User user = getCurrentUser();
        List<LectureLikes> lectureLikesList = lectureLikesRepository.findAllByUser(user);

        return lectureLikesList.stream()
                .map(like -> MyLectureListDto.fromEntity(like.getLecture()))
                .collect(Collectors.toList());
    }

    // ===== 공통 메서드 =====

    // 현재 로그인한 사용자 조회
    private User getCurrentUser() {
        UUID userId = AuthUtils.getCurrentUserId();
        return  userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

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