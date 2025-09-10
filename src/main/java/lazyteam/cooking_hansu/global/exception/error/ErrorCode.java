//package lazyteam.cooking_hansu.global.exception.error;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//
//@Getter
//@RequiredArgsConstructor
//public enum ErrorCode {
//
//    // ======================== 공통 예외 ========================
//    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
//    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
//    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
//    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입의 값입니다."),
//    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),
//    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "요청한 리소스를 찾을 수 없습니다."),
//    DUPLICATE_ENTITY(HttpStatus.CONFLICT, "C007", "이미 존재하는 데이터입니다."),
//
//    // ======================== 인증/인가 예외 ========================
//    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
//    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
//    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
//    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A004", "사용자를 찾을 수 없습니다."),
//    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A005", "잘못된 비밀번호입니다."),
//
//    // ======================== 사용자 관련 예외 ========================
//    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U001", "이미 사용 중인 이메일입니다."),
//    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U002", "이미 사용 중인 닉네임입니다."),
//    INVALID_OAUTH_TYPE(HttpStatus.BAD_REQUEST, "U003", "지원하지 않는 OAuth 타입입니다."),
//    WITHDRAWN_USER(HttpStatus.BAD_REQUEST, "U004", "탈퇴한 사용자입니다."),
//    BANNED_USER(HttpStatus.FORBIDDEN, "U005", "정지된 사용자입니다."),
//
//    // ======================== 강의 관련 예외 ========================
//    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "강의를 찾을 수 없습니다."),
//    LECTURE_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "L002", "이미 승인된 강의입니다."),
//    LECTURE_ALREADY_REJECTED(HttpStatus.BAD_REQUEST, "L003", "이미 거절된 강의입니다."),
//    LECTURE_NOT_APPROVED(HttpStatus.BAD_REQUEST, "L004", "승인되지 않은 강의입니다."),
//    LECTURE_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "L005", "강의 수정 권한이 없습니다."),
//    INVALID_LECTURE_PRICE(HttpStatus.BAD_REQUEST, "L006", "강의 가격은 0원 이상이어야 합니다."),
//    LECTURE_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "L007", "강의 제목은 100자 이하여야 합니다."),
//
//    // ======================== 게시글 관련 예외 ========================
//    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "게시글을 찾을 수 없습니다."),
//    BOARD_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "B002", "게시글 수정 권한이 없습니다."),
//    BOARD_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "B003", "이미 삭제된 게시글입니다."),
//    BOARD_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "B004", "게시글 제목은 255자 이하여야 합니다."),
//
//    // ======================== 댓글 관련 예외 ========================
//    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM001", "댓글을 찾을 수 없습니다."),
//    COMMENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "CM002", "댓글 수정 권한이 없습니다."),
//    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "CM003", "이미 삭제된 댓글입니다."),
//    COMMENT_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "CM004", "댓글 내용이 너무 깁니다."),
//
//    // ======================== 레시피 관련 예외 ========================
//    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "레시피를 찾을 수 없습니다."),
//    RECIPE_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "R002", "레시피 수정 권한이 없습니다."),
//    INVALID_COOK_TIME(HttpStatus.BAD_REQUEST, "R003", "조리 시간은 0분 이상이어야 합니다."),
//
//    // ======================== 결제 관련 예외 ========================
//    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제 정보를 찾을 수 없습니다."),
//    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "P002", "잔액이 부족합니다."),
//    PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "P003", "이미 완료된 결제입니다."),
//    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "P004", "결제 금액이 유효하지 않습니다."),
//    PAYMENT_METHOD_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "P005", "지원하지 않는 결제 방법입니다."),
//
//    // ======================== 파일 업로드 관련 예외 ========================
//    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "F001", "파일 크기가 제한을 초과했습니다."),
//    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "F002", "지원하지 않는 파일 형식입니다."),
//    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "파일 업로드에 실패했습니다."),
//    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F004", "파일을 찾을 수 없습니다."),
//
//    // ======================== 신고 관련 예외 ========================
//    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "RP001", "신고 정보를 찾을 수 없습니다."),
//    REPORT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "RP002", "이미 처리된 신고입니다."),
//    INVALID_REPORT_TYPE(HttpStatus.BAD_REQUEST, "RP003", "유효하지 않은 신고 유형입니다."),
//    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RP004", "자신의 콘텐츠는 신고할 수 없습니다."),
//
//    // ======================== 채팅 관련 예외 ========================
//    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채팅방을 찾을 수 없습니다."),
//    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CH002", "채팅 메시지를 찾을 수 없습니다."),
//    NOT_CHAT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "CH003", "채팅방 멤버가 아닙니다."),
//
//    // ======================== 알림 관련 예외 ========================
//    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "알림을 찾을 수 없습니다."),
//    NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "N002", "이미 읽은 알림입니다."),
//
//    // ======================== 검증 관련 예외 ========================
//    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "V001", "입력값 검증에 실패했습니다."),
//    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "V002", "필수 필드가 누락되었습니다."),
//    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "V003", "유효하지 않은 이메일 형식입니다."),
//    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "V004", "비밀번호 형식이 올바르지 않습니다."),
//    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "V005", "비밀번호가 일치하지 않습니다.");
//
//    private final HttpStatus status;
//    private final String code;
//    private final String message;
//
//    public static ErrorCode from(String code) {
//        for (ErrorCode errorCode : ErrorCode.values()) {
//            if (errorCode.getCode().equals(code)) {
//                return errorCode;
//            }
//        }
//        return INTERNAL_SERVER_ERROR;
//    }
//}
