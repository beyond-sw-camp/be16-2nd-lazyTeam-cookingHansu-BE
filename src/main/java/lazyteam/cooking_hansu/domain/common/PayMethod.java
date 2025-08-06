package lazyteam.cooking_hansu.domain.common;

public enum PayMethod {
    CARD,                       // 카드
    EASY_PAY,                   // 간편결제
    VIRTUAL_ACCOUNT,            // 가상계좌
    MOBILE_PHONE,               // 휴대폰
    TRANSFER,                   // 계좌이체
    CULTURE_GIFT_CERTIFICATE,   // 문화상품권
    BOOK_GIFT_CERTIFICATE,      // 도서문화상품권
    GAME_GIFT_CERTIFICATE,      // 게임문화상품권
    UNKNOWN;                    // 알 수 없는 값 처리용

    public static PayMethod from(String value) {
        return switch (value) {
            case "카드", "CARD" -> CARD;
            case "간편결제", "EASY_PAY" -> EASY_PAY;
            case "가상계좌", "VIRTUAL_ACCOUNT" -> VIRTUAL_ACCOUNT;
            case "휴대폰", "MOBILE_PHONE" -> MOBILE_PHONE;
            case "계좌이체", "TRANSFER" -> TRANSFER;
            case "문화상품권", "CULTURE_GIFT_CERTIFICATE" -> CULTURE_GIFT_CERTIFICATE;
            case "도서문화상품권", "BOOK_GIFT_CERTIFICATE" -> BOOK_GIFT_CERTIFICATE;
            case "게임문화상품권", "GAME_GIFT_CERTIFICATE" -> GAME_GIFT_CERTIFICATE;
            default -> {
                // 필요하다면 log.warn 추가 가능
                throw new IllegalArgumentException("지원하지 않는 결제 수단: " + value);
            }
        };
    }
}
