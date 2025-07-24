package lazyteam.cooking_hansu.global.exception.handler;

import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 잘못된 인자가 전달된 경우
     * ex) throw new IllegalArgumentException("...")
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage(), e);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Optional.get()에서 값이 없을 때
     * ex) Optional.empty().get()
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElementException(NoSuchElementException e) {
        log.error("NoSuchElementException: {}", e.getMessage(), e);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * 상태가 적절하지 않을 때
     * ex) 이미 삭제된 객체에 다시 접근하는 경우 등
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        log.error("IllegalStateException: {}", e.getMessage(), e);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.CONFLICT, e.getMessage()), HttpStatus.CONFLICT);
    }

    /**
     * null 참조 예외
     * ex) 객체가 null인데 메서드 호출했을 경우
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException: {}", e.getMessage(), e);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @Valid 실패: DTO 검증 실패 시 (첫 번째 에러만 추출)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "요청 데이터의 형식이 올바르지 않습니다.";
        log.warn("Validation 실패: {}", errorMessage);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.BAD_REQUEST, errorMessage), HttpStatus.BAD_REQUEST);
    }

    /**
     * 지원하지 않는 HTTP 메서드 호출 (예: GET만 허용인데 POST 요청)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("지원하지 않는 메서드: {}", e.getMessage(), e);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage()), HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * @RequestParam, @PathVariable 등의 바인딩 실패
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.error("핸들러 메서드 검증 실패: {}", e.getMessage(), e);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * JSON 파싱 에러 (ex: 잘못된 JSON 문법)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage(), e);
       return new ResponseEntity<>(ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * 쿼리 파라미터 누락 (ex: 필수 @RequestParam 없음)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingRequestParam(MissingServletRequestParameterException e) {
        log.error("필수 요청 파라미터 누락: {}", e.getParameterName());
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.BAD_REQUEST,
                String.format("필수 요청 파라미터 '%s'가 누락되었습니다.", e.getParameterName())), HttpStatus.BAD_REQUEST);
    }

    /**
     * 요청 타입이 잘못된 경우 (예: int 파라미터에 문자열 전달)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("요청 타입 불일치: {}={}", e.getName(), e.getValue());
        String errorMessage = String.format("요청 파라미터 '%s'의 타입이 '%s'이어야 합니다.", e.getName(), e.getRequiredType().getSimpleName());
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.BAD_REQUEST, errorMessage), HttpStatus.BAD_REQUEST);
    }

    /**
     * 지원하지 않는 MediaType (예: XML 요청을 보냈는데 JSON만 지원)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("지원하지 않는 미디어 타입: {}", e.getContentType());
        String errorMessage = String.format("지원하지 않는 미디어 타입: %s. 지원되는 타입은 %s입니다.",
                e.getContentType(), e.getSupportedMediaTypes());
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, errorMessage), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * 모든 처리되지 않은 예외 (최종 fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("Unhandled Exception: {}", e.getMessage(), e);
        return new ResponseEntity<>(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
