package lazyteam.cooking_hansu.global.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommonErrorDto {
    private int statusCode;
    private String statusMessage;
}
