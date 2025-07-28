package lazyteam.cooking_hansu.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class User {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long userId;
    private String email;
    private String password;
    private String name;
    private String phoneNumber;

}
