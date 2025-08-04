package lazyteam.cooking_hansu.domain.common;

public enum PayMethod {
//    TossAPI method 형식에 맞춰서 수정

    CARD,           // 카드
    VIRTUAL_ACCOUNT, // 가상계좌
    ACCOUNT_TRANSFER, // 계좌이체
    MOBILE,         // 휴대폰
    CULTURE_GIFT,   // 문화상품권
    FOREIGN;         // 해외결제

    public static PayMethod from(String value) {
        return switch (value) {
            case "카드" -> CARD;
            case "가상계좌" -> VIRTUAL_ACCOUNT;
            case "계좌이체" -> ACCOUNT_TRANSFER;
            case "휴대폰" -> MOBILE;
            case "문화상품권", "도서문화상품권", "게임문화상품권" -> CULTURE_GIFT;
            case "해외결제" -> FOREIGN;
            default -> throw new IllegalArgumentException("지원하지 않는 결제 수단: " + value);
        };
    }
}