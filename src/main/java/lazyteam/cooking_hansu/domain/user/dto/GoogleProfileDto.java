package lazyteam.cooking_hansu.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 자동 무시
public class GoogleProfileDto {
    private String sub; // 구글에서 사용하는 고유 사용자 ID
    private String email;
    private String picture;
}
