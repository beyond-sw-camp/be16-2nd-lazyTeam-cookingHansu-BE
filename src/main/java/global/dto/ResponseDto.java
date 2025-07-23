package global.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Data
@Builder
public class ResponseDto<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    public static <T> ResponseDto<T> ok(T data, HttpStatus status) {
        return ResponseDto.<T>builder()
                .success(true)
                .code(status.value())
                .data(data)
                .build();
    }

        public static <T> ResponseDto<T> fail(HttpStatus status, String message) {
        return ResponseDto.<T>builder()
                .success(false)
                .code(status.value())
                .message(message)
                .data(null)
                .build();
    }
}
