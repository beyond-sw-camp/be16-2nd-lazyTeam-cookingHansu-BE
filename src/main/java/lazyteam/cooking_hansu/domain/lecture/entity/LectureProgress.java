package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LectureProgress extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;   // 수강생

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_video_id", nullable = false)
    private LectureVideo lectureVideo;  // 어떤 영상인지

    @Column(nullable = false)
    @Builder.Default
    private Integer lastWatchedSecond = 0; // 마지막 시청 시점 (초 단위)

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false; // 완강 여부

    public void updateProgress(int second, int duration) {
        this.lastWatchedSecond = Math.min(second, duration);
        if (this.lastWatchedSecond >= duration) {
            this.completed = true;
        }
    }
}
