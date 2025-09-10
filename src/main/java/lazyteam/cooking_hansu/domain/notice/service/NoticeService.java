package lazyteam.cooking_hansu.domain.notice.service;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.admin.repository.AdminRepository;
import lazyteam.cooking_hansu.domain.notice.dto.*;
import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import lazyteam.cooking_hansu.domain.notice.repository.NoticeRepository;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AdminRepository adminRepository;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // 공지사항 등록
    public NoticeResDto createNotice(NoticeCreateDto noticeCreateDto) {
        if (noticeCreateDto.getTitle() == null || noticeCreateDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 입력입니다.");
        }
        if (noticeCreateDto.getContent() == null || noticeCreateDto.getContent().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 입력입니다.");
        }
        String email = "admin@naver.com";
        Admin admin = adminRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 관리자가 없습니다."));
        String imageUrl = null;
        if (noticeCreateDto.getNoticeImage() != null) {
            imageUrl = s3Uploader.upload(noticeCreateDto.getNoticeImage(), "notice/");
        }
        Notice notice = noticeCreateDto.toEntity(admin, imageUrl);
        noticeRepository.save(notice);


        // 모든 유저에게 알림
        List<User> allUsers = userRepository.findAll();
        
        for (User u : allUsers) {
                            notificationService.createAndDispatch(
                        SseMessageDto.builder()
                                .recipientId(u.getId())
                                .targetType(TargetType.NOTICE)
                                .targetId(notice.getId())
                                .content(notice.getTitle())
                                .createdAt(notice.getCreatedAt())
                                .build()
                );
        }
        return NoticeResDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(notice.getImageUrl())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    // 공지사항 전체 목록 조회
    @Transactional(readOnly = true)
    public Page<NoticeListDto> findAll(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findAll(pageable);
        return notices.map(NoticeListDto::fromEntity);
    }

    // 공지사항 상세조회
    @Transactional(readOnly = true)
    public NoticeDetailDto findById(UUID id) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 ID의 공지사항이 없습니다"));
        return NoticeDetailDto.fromEntity(notice);
    }

    // 공지사항 수정
    public NoticeResDto updateNotice(UUID id, NoticeUpdateDto noticeUpdateDto) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("수정할 공지사항이 없습니다."));
        if (noticeUpdateDto.getTitle() == null || noticeUpdateDto.getContent() == null) {
            throw new IllegalArgumentException("제목과 내용은 필수 입력입니다.");
        }

        String newImageUrl = notice.getImageUrl(); // 기존 이미지 URL 유지

        // 새 이미지가 있을 때만 기존 이미지 삭제하고 새 이미지 업로드
        if(noticeUpdateDto.getNoticeImage() != null){
            // 기존 이미지 삭제
            if (notice.getImageUrl() != null) {
                s3Uploader.delete(notice.getImageUrl());
            }
            // 새 이미지 업로드
            newImageUrl = s3Uploader.upload(noticeUpdateDto.getNoticeImage(), "notice/");
        }
        // 새 이미지가 없으면 기존 이미지 URL 그대로 유지

        notice.updateNotice(noticeUpdateDto, newImageUrl, notice.getAdmin());

        return NoticeResDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(notice.getImageUrl())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    // 공지사항 삭제
    public void deleteNotice(UUID id) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("삭제할 공지사항이 없습니다."));
        // S3에서 이미지 삭제
        if (notice.getImageUrl() != null && !notice.getImageUrl().isEmpty()) {
            s3Uploader.delete(notice.getImageUrl());
        }
        noticeRepository.delete(notice);
    }

}
