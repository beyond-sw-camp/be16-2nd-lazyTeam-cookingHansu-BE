package lazyteam.cooking_hansu.domain.user;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.BaseTimeEntity;
import lombok.AllArgsConstructor;
//import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 회원 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 회원 ID

    private String name;

    @Enumerated(EnumType.STRING)
    private OauthType oauthType;

    private String nickname;
    private String email;
    private String password;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private GeneralType generalType;

    @Enumerated(EnumType.STRING)
    private LoginStatus loginStatus;
}
