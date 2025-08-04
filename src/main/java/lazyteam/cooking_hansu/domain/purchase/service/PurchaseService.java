package lazyteam.cooking_hansu.domain.purchase.service;


import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.purchase.dto.TossPaymentConfirmDto;
import lazyteam.cooking_hansu.domain.purchase.repository.CartItemRepository;
import lazyteam.cooking_hansu.domain.purchase.repository.PaymentRepository;
import lazyteam.cooking_hansu.domain.purchase.repository.PurchasedLectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchasedLectureRepository purchasedLectureRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;

    @Value("${toss.secret-key}")
    private String widgetSecretKey;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    public JSONObject confirmPayment (TossPaymentConfirmDto tossPaymentConfirmDto) {
        try{
            JSONParser parser = new JSONParser();
            String orderId;
            String amount;
            String paymentKey;

            paymentKey = tossPaymentConfirmDto.getPaymentKey();
            orderId = tossPaymentConfirmDto.getOrderId();
            amount = tossPaymentConfirmDto.getAmount();

            JSONObject obj = new JSONObject();
            obj.put("orderId", orderId);
            obj.put("amount", amount);
            obj.put("paymentKey", paymentKey);

            // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
            // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.

            Base64.Encoder encoder = Base64.getEncoder();
            byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
            String authorizations = "Basic " + new String(encodedBytes);

            // 결제를 승인하면 결제수단에서 금액이 차감돼요.
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(obj.toString().getBytes("UTF-8"));

            int code = connection.getResponseCode();
            boolean isSuccess = code == 200;

            InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            responseStream.close();

//            결제 성공시






            return jsonObject;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            log.info(e.getMessage());
            throw new RuntimeException("토스 결제 승인 처리 중 오류 발생", e);
        }


    }



}
