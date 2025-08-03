package lazyteam.cooking_hansu.domain.notice.service;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.admin.repository.AdminRepository;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeCreateDto;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeDetailDto;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeListDto;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeUpdateDto;
import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import lazyteam.cooking_hansu.domain.notice.repository.NoticeRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AdminRepository adminRepository;
    private final S3Uploader s3Uploader;

    // 공지사항 등록
    public void createNotice(NoticeCreateDto noticeCreateDto) {
        if (noticeCreateDto.getContent() == null || noticeCreateDto.getContent().isEmpty()) {
            throw new IllegalArgumentException("제목과 내용은 필수 입력입니다.");
        }
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String email = "admin@naver.com";
        Admin admin = adminRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 관리자가 없습니다."));
        String imageUrl = null;
        if (noticeCreateDto.getNoticeImage() != null) {
            imageUrl = s3Uploader.upload(noticeCreateDto.getNoticeImage(), "notice/");
        }
        Notice notice = noticeCreateDto.toEntity(admin, imageUrl);
        noticeRepository.save(notice);
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
    public void updateNotice(UUID id, NoticeUpdateDto noticeUpdateDto) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("수정할 공지사항이 없습니다."));
        if (noticeUpdateDto.getTitle() == null || noticeUpdateDto.getContent() == null) {
            throw new IllegalArgumentException("제목과 내용은 필수 입력입니다.");
        }
        // 기존 이미지 삭제
        if (notice.getImageUrl() != null) {
            s3Uploader.delete(notice.getImageUrl());
        }

        String newImageUrl = null;

        if(noticeUpdateDto.getNoticeImage() !=null){
            newImageUrl = s3Uploader.upload(noticeUpdateDto.getNoticeImage(), "notice/");
        }

        notice.updateNotice(noticeUpdateDto, newImageUrl, notice.getAdmin());
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
