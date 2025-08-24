package lazyteam.cooking_hansu.domain.purchase.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.common.enums.PayMethod;
import lazyteam.cooking_hansu.domain.common.enums.PaymentStatus;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.purchase.dto.TossPaymentConfirmDto;
import lazyteam.cooking_hansu.domain.purchase.dto.TossPrepayDto;
import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
import lazyteam.cooking_hansu.domain.purchase.entity.Payment;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lazyteam.cooking_hansu.domain.purchase.entity.TossPrePay;
import lazyteam.cooking_hansu.domain.purchase.repository.CartItemRepository;
import lazyteam.cooking_hansu.domain.purchase.repository.PaymentRepository;
import lazyteam.cooking_hansu.domain.purchase.repository.PurchasedLectureRepository;
import lazyteam.cooking_hansu.domain.purchase.repository.TossPrepayRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchasedLectureRepository purchasedLectureRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final TossPrepayRepository tossPrepayRepository;
    private final NotificationService notificationService;

    @Value("${toss.secret-key}")
    private String widgetSecretKey;



    public void prepaymentSave(TossPrepayDto tossPrepayDto) {
        tossPrepayRepository.save(tossPrepayDto.toEntity());
        log.info("사전정보등록");
    }



    // 프론트엔드에서 결제창에서 확인을 누르면 Controller를 통해 dto로 정보를 받아서 이 로직 실행
    public JSONObject confirmPayment(TossPaymentConfirmDto tossPaymentConfirmDto) {

        // 테스트용 유저 (로그인 기능이 붙으면 SecurityContextHolder로 대체)
        UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("테스트 유저가 없습니다."));

        JSONParser parser = new JSONParser();

        TossPrePay tossPrePay = tossPrepayRepository.findByOrderId(tossPaymentConfirmDto.getOrderId())
                .orElseThrow(()-> new EntityNotFoundException("해당 주문번호가 없습니다."));

        String orderId = tossPrePay.getOrderId();
        Long amount = tossPrePay.getAmount();
        String paymentKey = tossPaymentConfirmDto.getPaymentKey();

        try {
            // 1. Toss 요청 본문 생성
            JSONObject obj = new JSONObject();
            obj.put("orderId", orderId);
            obj.put("amount", amount);
            obj.put("paymentKey", paymentKey);

            // 2. Basic 인증 헤더 구성 (시크릿 키를 base64 인코딩)
            Base64.Encoder encoder = Base64.getEncoder();
            String authHeader = "Basic " + new String(
                    encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8))
            );
            // 3. Toss 결제 승인 API 호출
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            // 프론트에서 SDK 호출 전 받아놓았던 값과 paymentkey(UUID로 생성한 값, dto로 받음)을 직렬화 하여 toss로 보냄
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            // code가 200이면 성공이라는 뜻.
            boolean isSuccess = code == 200;


            // 4. 응답 파싱 (성공시 InputStream, 실패시 ErrorStream 사용)
            InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
            JSONObject jsonObject;
            try (Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
                jsonObject = (JSONObject) parser.parse(reader);
            }
            log.info("응답파싱성공");

            // 5. 결제 성공 시 후처리 로직
            if (isSuccess) {
                try {
                    log.info("저장된 강의 ");
                    List<UUID> lectureIds = tossPrePay.getLectureIds();

                    log.info(jsonObject.toJSONString());

                    // 5-1. 결제 정보 저장
                    Payment payment = Payment.builder()
                            .user(user)
                            .paymentKey(paymentKey)
                            .orderId(orderId)
                            .paidAmount(amount)
                            .payMethod(PayMethod.from((String) jsonObject.get("method")))
                            .paidAt(OffsetDateTime.parse((String) jsonObject.get("approvedAt")).toLocalDateTime())
                            .status(PaymentStatus.SUCCESS)
                            .build();
                    paymentRepository.save(payment);
                    log.info("결제정보 저장 완료");

                    // 5-2. 구매 내역 저장, 강의 카운트 추가
                    for (UUID lectureId : lectureIds) {
                        Lecture lecture = lectureRepository.findById(lectureId)
                                .orElseThrow(() -> new EntityNotFoundException("강의가 없습니다: " + lectureId));

                        PurchasedLecture purchased = PurchasedLecture.builder()
                                .user(user)
                                .lecture(lecture)
                                .payment(payment)
                                .lectureTitleSnapshot(lecture.getTitle())
                                .priceSnapshot(Integer.parseInt(jsonObject.get("balanceAmount").toString()))
                                .build();
                        purchasedLectureRepository.save(purchased);

                        lecture.setPurchaseCount(lecture.getPurchaseCount() + 1);
                    }
                    log.info("구매내역저장완료");

                    // 5-3. 장바구니에서 해당 강의만 삭제
                    deleteSelected(user.getId(), lectureIds);
                    log.info("강의삭제완료");

                    // 결제 완료 알림 발송
                    StringBuilder lectureNames = new StringBuilder();
                    for (int i = 0; i < lectureIds.size(); i++) {
                        UUID lectureId = lectureIds.get(i);
                        Lecture lecture = lectureRepository.findById(lectureId)
                                .orElseThrow(() -> new EntityNotFoundException("강의가 없습니다: " + lectureId));
                        lectureNames.append(lecture.getTitle());
                        if (i < lectureIds.size() - 1) {
                            lectureNames.append(", ");
                        }
                    }
                    
                    String notificationContent = String.format("결제가 완료되었습니다: %s (총 %,d원)", 
                            lectureNames.toString(), amount);
                    
                    SseMessageDto sseMessageDto = SseMessageDto.builder()
                            .recipientId(user.getId())
                            .targetType(TargetType.PAYMENT)
                            .targetId(payment.getId())
                            .content(notificationContent)
                            .build();
                    
                    notificationService.createAndDispatch(sseMessageDto);

                } catch (Exception e) {
                    log.error("결제 후처리 중 오류 발생", e);
                    throw new RuntimeException("결제 후처리 중 오류 발생: " + e.getMessage(), e);
                }
            }
            log.info("리턴 전" + jsonObject.toJSONString());

            return jsonObject;

        } catch (IOException | ParseException e) {
            log.error("토스 결제 승인 처리 중 예외 발생", e);
            throw new RuntimeException("토스 결제 승인 처리 중 오류 발생", e);
        }
    }


    // 장바구니에서 삭제
    public void deleteSelected(UUID userId, List<UUID> lectureIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        List<CartItem> items = new ArrayList<>();

        for(UUID id : lectureIds) {
            items.add(cartItemRepository.findByUserAndLectureId(user, id));
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("삭제할 장바구니 항목이 없습니다.");
        }

        cartItemRepository.deleteAll(items);
    }



}
