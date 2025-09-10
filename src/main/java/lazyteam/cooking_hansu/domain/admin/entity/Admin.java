package lazyteam.cooking_hansu.domain.admin.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class Admin extends BaseIdAndTimeEntity {

    @Column(name = "admin_name", nullable = false, length = 50)
    private String name; // 관리자 이름

    @Column(name = "admin_email", nullable = false, length = 50)
    private String email; // 관리자 이메일

    @Column(name = "admin_password", nullable = false, length = 100)
    private String password; // 관리자 비밀번호

}
