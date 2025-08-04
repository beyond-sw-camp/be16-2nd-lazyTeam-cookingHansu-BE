package lazyteam.cooking_hansu.domain.purchase.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.purchase.dto.TossPaymentConfirmDto;
import lazyteam.cooking_hansu.domain.purchase.service.PurchaseService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/purchase")
@RestController
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping(value = "/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody @Valid TossPaymentConfirmDto tossPaymentConfirmDto) {


        JSONObject paymentResult = purchaseService.confirmPayment(tossPaymentConfirmDto);

        return new ResponseEntity<>(ResponseDto.ok(paymentResult,HttpStatus.OK), HttpStatus.OK);
    }


}