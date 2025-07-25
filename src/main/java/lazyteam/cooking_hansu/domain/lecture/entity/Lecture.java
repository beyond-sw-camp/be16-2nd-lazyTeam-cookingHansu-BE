package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder

public class Lecture {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long lectureId;

    //fk설정 필요(조회용)
    private Long submittedId;
    private Long approveAdminId;
    private Long rejectAdminId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
    private
}
