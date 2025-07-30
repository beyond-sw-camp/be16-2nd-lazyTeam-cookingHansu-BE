package lazyteam.cooking_hansu.global.exception.handler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ======================== 인증(Auth) & 인가(Authorization) ========================
     */

    // 인증 실패 (로그인 실패, 잘못된 토큰 등)
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("[AuthenticationException] {}", e.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다. 다시 로그인 해주세요.");
    }

    // 인증 정보 부족
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<?> handleInsufficientAuthentication(InsufficientAuthenticationException e) {
        log.warn("[InsufficientAuthenticationException] {}", e.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, "인증 정보가 부족합니다.");
    }

    // 사용자명을 찾을 수 없음
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException e) {
        log.warn("[UsernameNotFoundException] {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.");
    }

    // 인가 실패 (권한 없음)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        log.warn("[AccessDeniedException] {}", e.getMessage());
        return buildError(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    /**
     * ======================== Entity/리소스 관련 예외 ========================
     */

    // JPA Entity 조회 실패
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException e) {
        log.error("[EntityNotFoundException] {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.");
    }

    // Optional.get() 에서 값 없음
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElement(NoSuchElementException e) {
        log.error("[NoSuchElementException] {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다.");
    }

    // JPA 결과가 없음
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<?> handleEmptyResultDataAccess(EmptyResultDataAccessException e) {
        log.error("[EmptyResultDataAccessException] {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, "요청한 데이터가 존재하지 않습니다.");
    }

    /**
     * ======================== DB 관련 예외 ========================
     */

    // DB 무결성 제약 조건 위반 (UniqueKey, FK 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException] {}", e.getMessage());
        return buildError(HttpStatus.CONFLICT, "데이터 무결성 제약 조건을 위반했습니다.");
    }

    // 중복 키 에러
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleDuplicateKey(DuplicateKeyException e) {
        log.error("[DuplicateKeyException] {}", e.getMessage());
        return buildError(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다.");
    }

    // 트랜잭션 시스템 에러
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<?> handleTransactionSystem(TransactionSystemException e) {
        log.error("[TransactionSystemException] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 처리 중 오류가 발생했습니다.");
    }

    // JDBC 에러 (SQLSyntaxError, Connection 에러 등 포함)
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handleSQLException(SQLException e) {
        log.error("[SQLException] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다.");
    }

    /**
     * ======================== 런타임 예외 ========================
     */

    // 잘못된 인자 전달
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] {}", e.getMessage(), e);
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 잘못된 상태에서 로직 호출
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        log.error("[IllegalStateException] {}", e.getMessage(), e);
        return buildError(HttpStatus.CONFLICT, e.getMessage());
    }

    // Null 참조
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointer(NullPointerException e) {
        log.error("[NullPointerException]", e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 Null 참조 오류가 발생했습니다.");
    }

    // 타임아웃
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<?> handleTimeout(TimeoutException e) {
        log.error("[TimeoutException] {}", e.getMessage());
        return buildError(HttpStatus.REQUEST_TIMEOUT, "요청 처리 시간이 초과되었습니다.");
    }

    /**
     * ======================== 요청/검증 관련 예외 ========================
     */

    // DTO @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "요청 데이터가 유효하지 않습니다.";
        log.error("[Validation 실패] {}", errorMessage);
        return buildError(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // Binding 실패 (단순 필드 매핑 에러 포함)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "요청 데이터가 유효하지 않습니다.";
        log.error("[BindException] {}", errorMessage);
        return buildError(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // PathVariable, RequestParam 검증 실패
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.error("[HandlerMethodValidationException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "요청 파라미터가 유효하지 않습니다.");
    }

    // @Validated 메서드 파라미터 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException e) {
        log.error("[ConstraintViolationException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다.");
    }

    // 필수 요청 파라미터(@RequestParam) 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterException] {}", e.getParameterName());
        return buildError(HttpStatus.BAD_REQUEST, String.format("필수 요청 파라미터 '%s'가 누락되었습니다.", e.getParameterName()));
    }

    // PathVariable, RequestParam 타입 불일치 (int에 문자열 등)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("요청 파라미터 '%s'의 타입이 '%s'이어야 합니다.",
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "알 수 없음");
        log.error("[MethodArgumentTypeMismatchException] {}", errorMessage);
        return buildError(HttpStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * ======================== HTTP 요청 처리 예외 ========================
     */

    // 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("[HttpRequestMethodNotSupportedException] {}", e.getMessage());
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
    }

    // 지원하지 않는 미디어 타입 (JSON만 지원하는데 XML 요청)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("[HttpMediaTypeNotSupportedException] {}", e.getContentType());
        return buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                String.format("지원하지 않는 미디어 타입: %s. 지원되는 타입은 %s입니다.",
                        e.getContentType(), e.getSupportedMediaTypes()));
    }

    // JSON 파싱 에러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "JSON 요청을 읽을 수 없습니다.");
    }

    // 요청 body나 multipart 데이터 누락
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingServletRequestPart(MissingServletRequestPartException e) {
        log.error("[MissingServletRequestPartException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "요청에 필요한 데이터가 누락되었습니다.");
    }

    // 핸들러를 찾을 수 없음 (404)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException e) {
        log.error("[NoHandlerFoundException] {} {}", e.getHttpMethod(), e.getRequestURL());
        return buildError(HttpStatus.NOT_FOUND, "요청한 API 엔드포인트를 찾을 수 없습니다.");
    }

    /**
     * ======================== 파일 업로드 관련 예외 ========================
     */

    // 파일 업로드 크기 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.error("[MaxUploadSizeExceededException] {}", e.getMessage());
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 파일 크기가 제한을 초과했습니다.");
    }

    // 멀티파트 처리 에러
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipartException(MultipartException e) {
        log.error("[MultipartException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "파일 업로드 처리 중 오류가 발생했습니다.");
    }



    /**
     * ======================== IO 관련 예외 ========================
     */

    // IO 에러 (파일 읽기/쓰기 실패)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException e) {
        log.error("[IOException] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");
    }

    /**
     * ======================== 비즈니스 로직 관련 예외 ========================
     */

    // 강의 승인 상태 관련 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleLectureApprovalException(IllegalArgumentException e) {
        if (e.getMessage().contains("승인") || e.getMessage().contains("강의")) {
            log.warn("[LectureApprovalException] {}", e.getMessage());
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        // 다른 IllegalArgumentException은 기존 핸들러로
        throw e;
    }

    // 결제 관련 예외
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handlePaymentException(IllegalStateException e) {
        if (e.getMessage().contains("결제") || e.getMessage().contains("가격")) {
            log.warn("[PaymentException] {}", e.getMessage());
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        // 다른 IllegalStateException은 기존 핸들러로
        throw e;
    }

    /**
     * ======================== Fallback (그 외 모든 예외) ========================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("[Unhandled Exception] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    /**
     * ======================== 공통 ResponseEntity 생성 ========================
     */
    private ResponseEntity<?> buildError(HttpStatus status, String message) {
        return new ResponseEntity<>(ResponseDto.fail(status, message), status);
    }
}
