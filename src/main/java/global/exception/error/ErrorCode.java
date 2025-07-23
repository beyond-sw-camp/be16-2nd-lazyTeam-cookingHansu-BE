//package global.exception.error;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//
//@Getter
//@RequiredArgsConstructor
//public enum ErrorCode {
//    // 🔸 공통 에러 코드
//    ILLEGAL_ARGUMENT(10000, HttpStatus.BAD_REQUEST, "잘못된 인자입니다."),
//    NO_SUCH_ELEMENT(10001, HttpStatus.NOT_FOUND, "해당 요소를 찾을 수 없습니다."),
//    ILLEGAL_STATE(10002, HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 상태입니다."),
//    NULL_POINTER(10003, HttpStatus.INTERNAL_SERVER_ERROR, "널 포인터 예외가 발생했습니다."),
//
//    // 🔹 400 Bad Request (클라이언트의 잘못된 요청)
//    INVALID_INPUT_VALUE(40000, HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
//    MISSING_REQUEST_PARAMETER(40001, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
//    INVALID_TYPE_VALUE(40002, HttpStatus.BAD_REQUEST, "입력 타입이 올바르지 않습니다."),
//    VALIDATION_ERROR(40003, HttpStatus.BAD_REQUEST, "요청 데이터의 유효성 검사에 실패했습니다."),
//    JSON_PARSE_ERROR(40004, HttpStatus.BAD_REQUEST, "요청 JSON 파싱에 실패했습니다."),
//
//    // 🔹 401 Unauthorized (인증 실패 관련)
//    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
//    INVALID_TOKEN(40101, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
//    EXPIRED_TOKEN(40102, HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
//    ACCESS_TOKEN_REQUIRED(40103, HttpStatus.UNAUTHORIZED, "AccessToken이 필요합니다."),
//    REFRESH_TOKEN_REQUIRED(40104, HttpStatus.UNAUTHORIZED, "RefreshToken이 필요합니다."),
//    LOGIN_REQUIRED(40105, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
//
//    // 🔹 403 Forbidden (인가 실패 관련)
//    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
//    ACCESS_DENIED(40301, HttpStatus.FORBIDDEN, "권한이 없습니다."),
//
//    // 🔹 404 Not Found (리소스를 찾을 수 없음)
//    NOT_FOUND(40400, HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
//    NOT_FOUND_USER(40401, HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
//    NOT_FOUND_RESOURCE(40402, HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다."),
//    NOT_FOUND_END_POINT(40403, HttpStatus.NOT_FOUND, "존재하지 않는 API 엔드포인트입니다."),
//
//    // 🔹 405 Method Not Allowed
//    METHOD_NOT_ALLOWED(40500, HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메소드입니다."),
//
//    // 🔹 409 Conflict (중복/충돌)
//    CONFLICT(40900, HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
//    ALREADY_EXIST_USER(40901, HttpStatus.CONFLICT, "이미 가입된 유저입니다."),
//    ALREADY_EXIST_ID(40902, HttpStatus.CONFLICT, "이미 존재하는 ID입니다."),
//    DUPLICATE_RESOURCE(40903, HttpStatus.CONFLICT, "리소스가 중복되었습니다."),
//
//    // 🔹 415 Unsupported Media Type
//    UNSUPPORTED_MEDIA_TYPE(41500, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다."),
//
//    // 🔹 422 Unprocessable Entity
//    UNPROCESSABLE_ENTITY(42200, HttpStatus.UNPROCESSABLE_ENTITY, "처리할 수 없는 요청입니다."),
//
//    // 🔹 429 Too Many Requests
//    TOO_MANY_REQUESTS(42900, HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
//
//    // 🔹 500 Internal Server Error (서버 내부 에러)
//    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러입니다."),
//    DATABASE_ERROR(50001, HttpStatus.INTERNAL_SERVER_ERROR, "DB 처리 중 에러가 발생했습니다."),
//    IO_ERROR(50002, HttpStatus.INTERNAL_SERVER_ERROR, "입출력 처리 중 에러가 발생했습니다."),
//
//    // 🔸 사용자 도메인 관련 에러
//    USER_NOT_FOUND(40410, HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
//    EMAIL_ALREADY_USED(40910, HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
//    PASSWORD_MISMATCH(40010, HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
//    USER_NOT_ACTIVE(40310, HttpStatus.FORBIDDEN, "비활성화된 사용자입니다."),
//
//    // 🔸 인증/인가 관련
//    AUTH_CODE_INVALID(40110, HttpStatus.UNAUTHORIZED, "인증 코드가 유효하지 않습니다."),
//    OAUTH_PROVIDER_ERROR(50010, HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 처리 중 에러 발생"),
//
//    // 🔸 파일 업로드 관련
//    FILE_UPLOAD_FAILED(50020, HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
//    FILE_NOT_FOUND(40420, HttpStatus.NOT_FOUND, "요청한 파일이 존재하지 않습니다."),
//    UNSUPPORTED_FILE_EXTENSION(40020, HttpStatus.BAD_REQUEST, "지원하지 않는 파일 확장자입니다."),
//
//    // 🔸 게시글/댓글 등 컨텐츠 관련
//    POST_NOT_FOUND(40430, HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
//    COMMENT_NOT_FOUND(40431, HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
//    UNAUTHORIZED_POST_ACCESS(40330, HttpStatus.FORBIDDEN, "게시글 접근 권한이 없습니다.");
//
//    // 🔸 공통 필드
//    private final int code;               // 프론트와의 명확한 식별을 위한 고유 에러 코드
//    private final HttpStatus httpStatus; // HTTP 상태 코드
//    private final String message;        // 사용자/개발자용 메시지
//}
