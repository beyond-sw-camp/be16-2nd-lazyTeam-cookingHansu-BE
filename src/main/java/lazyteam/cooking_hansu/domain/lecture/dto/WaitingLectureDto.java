package lazyteam.cooking_hansu.domain.lecture.dto;

import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WaitingLectureDto {
    private UUID id; // 강의 ID
    private String title; // 강의 제목
    private String description; // 강의 설명
    private String imageUrl; // 강의 이미지 URL
    private CategoryEnum category; // 강의 카테고리
    private String instructorName; // 강사 이름
    private ApprovalStatus status; // 강의 상태 (예: 승인 대기중, 승인됨, 거절됨 등)
    private Integer price; // 강의 가격
    private Integer duration; // 강의 시간 (분 단위)
}
