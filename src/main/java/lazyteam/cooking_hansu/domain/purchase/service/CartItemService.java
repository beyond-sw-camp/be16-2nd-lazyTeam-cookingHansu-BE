package lazyteam.cooking_hansu.domain.purchase.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.purchase.dto.CartDeleteAllDto;
import lazyteam.cooking_hansu.domain.purchase.dto.CartDeleteOneDto;
import lazyteam.cooking_hansu.domain.purchase.dto.CartItemAddDto;
import lazyteam.cooking_hansu.domain.purchase.dto.CartItemListDto;
import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
import lazyteam.cooking_hansu.domain.purchase.repository.CartItemRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    public void addCart(CartItemAddDto dto) {


//        테스트용 유저 세팅
        UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("테스트 유저가 없습니다."));

        for(UUID id : dto.getLectureIds()) {
            Lecture lecture = lectureRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("해당 ID의 강의가 없습니다."));

            if(cartItemRepository.existsByUserAndLecture(user,lecture)) {
                throw new IllegalArgumentException("이미 장바구니에 담긴 강의입니다.");
            }

            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .lecture(lecture)
                    .build();
            cartItemRepository.save(cartItem);
        }

    }

    public List<CartItemListDto> findById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 유저 없습니다."));

        return cartItemRepository.findAllByUser(user).stream().map(CartItemListDto::fromEntity).toList();

    }


    public void deleteOne(CartDeleteOneDto cartDeleteOneDto) {
        //        테스트용 유저 세팅
        UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("테스트 유저가 없습니다."));

        Lecture lecture = lectureRepository.findById(cartDeleteOneDto.getLectureId())
                .orElseThrow(() -> new EntityNotFoundException("강의 없음"));

        CartItem cartItem = cartItemRepository.findByUserAndLecture(user, lecture)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 항목 없음"));

        cartItemRepository.delete(cartItem);
    }


    public void deleteAll(CartDeleteAllDto cartDeleteAllDto) {
        User user = userRepository.findById(cartDeleteAllDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));
        cartItemRepository.deleteAllByUser(user);
    }

}
