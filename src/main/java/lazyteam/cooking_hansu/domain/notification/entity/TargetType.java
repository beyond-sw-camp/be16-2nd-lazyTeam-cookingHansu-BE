package lazyteam.cooking_hansu.domain.notification.entity;

public enum TargetType {
    POSTCOMMENT,     // 게시글에 단 댓글 알림
    QNACOMMENT,
    REPLY,       // 댓글에 단 대댓글 알림
    APPROVAL,    // 회원가입 승인 알림
    CHAT,        // 실시간 채팅 알림
    PAYMENT,     // 강의 결제 성공 알림
    NOTICE       // 새 공지사항 등록 알림

}

