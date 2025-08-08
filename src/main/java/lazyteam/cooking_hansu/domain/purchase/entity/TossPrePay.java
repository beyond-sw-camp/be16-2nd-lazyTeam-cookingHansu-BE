package lazyteam.cooking_hansu.domain.purchase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString

public class TossPrePay {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "주문번호는 필수입니다.")
    @Column(unique = true)
    private String orderId;

    @NotNull(message = "주문금액은 필수입니다.")
    private Long amount;

    @NotNull(message = "강의 ID는 필수입니다.")
    private List<UUID> lectureIds;



}
