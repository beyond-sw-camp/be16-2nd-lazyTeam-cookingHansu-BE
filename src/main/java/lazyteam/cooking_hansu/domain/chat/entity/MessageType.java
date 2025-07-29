package lazyteam.cooking_hansu.domain.chat.entity;

public enum MessageType {
    TEXT("TEXT"),  // 일반 텍스트 메시지
    IMAGE("IMAGE"), // 이미지 메시지
    FILE("FILE"), // 파일 메시지
    VIDEO("VIDEO"), // 비디오 메시지
    AUDIO("AUDIO"); // 오디오 메시지

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
