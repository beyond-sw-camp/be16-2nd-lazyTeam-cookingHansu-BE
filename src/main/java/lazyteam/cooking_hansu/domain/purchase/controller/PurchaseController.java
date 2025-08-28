package lazyteam.cooking_hansu.domain.purchase.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.purchase.dto.TossPaymentConfirmDto;
import lazyteam.cooking_hansu.domain.purchase.dto.TossPrepayDto;
import lazyteam.cooking_hansu.domain.purchase.service.PurchaseService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RequestMapping("/purchase")
@RestController
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;



    @PostMapping("/prepay")
    public ResponseEntity<?> prepaymentSave(@RequestBody TossPrepayDto tossPrepayDto) {
        purchaseService.prepaymentSave(tossPrepayDto);
        return new ResponseEntity<>(ResponseDto.ok(tossPrepayDto,HttpStatus.OK),HttpStatus.OK);
    }


    @PostMapping("/confirm")
    public JSONObject confirmPayment(@RequestBody @Valid TossPaymentConfirmDto tossPaymentConfirmDto) {

        return purchaseService.confirmPayment(tossPaymentConfirmDto);
    }

    @GetMapping("/history/{lectureId}")
    public ResponseEntity<?> payHistory (@PathVariable UUID lectureId) {
        return new ResponseEntity<>(ResponseDto.ok(purchaseService.payHistory(lectureId),HttpStatus.OK),HttpStatus.OK);
    }



}