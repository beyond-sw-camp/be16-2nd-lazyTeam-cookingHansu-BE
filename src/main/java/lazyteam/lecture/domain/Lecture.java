package lazyteam.lecture.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.ValueGenerationType;

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
    private Long rejectAdminId;
}
