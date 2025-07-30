package lazyteam.cooking_hansu.domain.user.entity.common;

import jakarta.persistence.*;
//import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.board.entity.Board;
import lazyteam.cooking_hansu.domain.chat.entity.ChatMessage;
import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.comment.entity.Comment;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.interaction.entity.Bookmark;
import lazyteam.cooking_hansu.domain.interaction.entity.Likes;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureQna;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lazyteam.cooking_hansu.domain.notification.entity.Notification;
import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
import lazyteam.cooking_hansu.domain.purchase.entity.Payment;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.report.entity.Report;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 공통 회원 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // 회원 ID

    @Column(nullable = false)
    private String name; // 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OauthType oauthType; // 소셜 로그인 유형 (KAKAO, GOOGLE, NAVER)

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column(unique = true)
    private String email; // 이메일

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(length = 512, nullable = false)
    private String profileImageUrl; // 프로필 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.GENERAL; // 회원 역활 (GENERAL, CHEF, OWNER, BOTH, ADMIN)

    @Enumerated(EnumType.STRING)
    private GeneralType generalType; // 일반 회원 유형 (STUDENT, HOUSEWIFE, LIVINGALONE, ETC)

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private LoginStatus loginStatus = LoginStatus.LOGGED_IN; // 로그인 상태 (LOGGED_IN, LOGGED_OUT, WITHDRAWN, BANNED)

    // 관계 설정은 추후 협의해서 추가 예정
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Chef chef; // 요식업 종사자 1:1 관계

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Business business; // 요식업 자영업자 1:1 관계

    // 관리자(Administrator) 테이블에 FK admin_id로 1:1 연관 관계 (단방향)
    /*@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Admin admin;*/

    // 알림 테이블 1:N 관계
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Notification> recipientList = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Notification> senderList = new ArrayList<>();

    // 채팅 메시지(ChatMessage) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    // 메시지 읽음(MessageReadStatus) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatReadStatus> chatReadStatusList = new ArrayList<>();

    // 채팅 참여(ChatParticipant) 테이블에 FK user_id로 1:N 식별 관계
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatParticipant> chatParticipantList = new ArrayList<>();

    // 강의(Lecture) 테이블에 FK submitted_id(요청자 Id)로 1:N 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lecture> lectureList = new ArrayList<>();

    // 강의 리뷰(LectureReview> 테이블에 FK writer_id(작성자 ID)로 1:n 연관 관계
    @OneToMany(mappedBy = "writerId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LectureReview> lectureReviewList = new ArrayList<>();

    // 강의 QnA 댓글(LectureQna) 테이블에 FK question_user_id(질문자ID)와 FK answer_user_id(답변자ID)로 1:N 연관 관계
    @OneToMany(mappedBy = "questionUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LectureQna> questionUserList = new ArrayList<>(); // 질문자

    @OneToMany(mappedBy = "answerUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LectureQna> answerUserList = new ArrayList<>(); // 답변자

    // 장바구니 내역(CartItem) 테이블에 FK user_id로 1:1 연관 관계
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private CartItem cartItem;

    // 결제(Payment) 테이블에 FK user_id로 1:n 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> paymentList = new ArrayList<>();

    // 결제한 강의(PurchasedLecture) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchasedLecture> purchasedLectureList = new ArrayList<>();

    // 레시피(Recipe) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Recipe> recipeList = new ArrayList<>();

    // 레시피 공유 게시글(Board) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Board> boardList = new ArrayList<>();

    // 댓글(Comment) 테이블에 FK comment_user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> commentList = new ArrayList<>();

    // 좋아요(Likes) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Likes> likesList = new ArrayList<>();

    // 북마크(Bookmark) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bookmark> bookmarkList = new ArrayList<>();

    // 신고(Report) 테이블에 FK user_id로 1:N 연관 관계
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Report> reportList = new ArrayList<>();

    // 비즈니스 메서드 관련은 추후 구현 예정

}
