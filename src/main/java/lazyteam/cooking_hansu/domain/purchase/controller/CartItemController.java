package lazyteam.cooking_hansu.domain.purchase.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.purchase.dto.CartItemAddDto;
import lazyteam.cooking_hansu.domain.purchase.dto.CartItemListDto;
import lazyteam.cooking_hansu.domain.purchase.service.CartItemService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

//    장바구니 담기
    @PostMapping("/add")
    public ResponseEntity<?> addCart(@Valid @RequestBody CartItemAddDto dto) {
        cartItemService.addCart(dto);
        return new ResponseEntity<>(ResponseDto.ok(HttpStatus.CREATED.value() + " 장바구니 저장에 성공했습니다.", HttpStatus.CREATED), HttpStatus.CREATED);
    }

//    장바구니 조회(유저 id로 조회)
    @GetMapping("/list/{id}")
    public ResponseEntity<?> CartList(@PathVariable UUID id) {
        List<CartItemListDto> dto =  cartItemService.findById(id);
        return new ResponseEntity<>(ResponseDto.ok(dto,HttpStatus.OK),HttpStatus.OK);
    }

//    장바구니 단건 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteOne(
            @RequestParam UUID userId,
            @RequestParam UUID lectureId) {
        cartItemService.deleteOne(userId, lectureId);
        return new ResponseEntity<>(ResponseDto.ok("단건삭제 완료",HttpStatus.OK),HttpStatus.OK);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAll(@RequestParam UUID userId) {
        cartItemService.deleteAll(userId);
        return new ResponseEntity<>(ResponseDto.ok("전체삭제 완료",HttpStatus.OK),HttpStatus.OK);
    }

}
