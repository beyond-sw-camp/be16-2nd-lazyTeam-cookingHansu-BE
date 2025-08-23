package lazyteam.cooking_hansu.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 조회수 정책 설정
 */
@Component
@ConfigurationProperties(prefix = "app.view-count")
@Getter
public class ViewCountConfig {
    
    /**
     * 비회원도 조회수 증가 허용 여부
     * true: 비회원도 조회수 증가
     * false: 회원만 조회수 증가
     */
    private boolean allowGuestViewCount = false;
    
    /**
     * IP 기반 중복 방지 활성화 여부 (비회원용)
     * true: IP 기반으로 중복 방지
     * false: IP 중복 방지 없음
     */
    private boolean enableIpDuplication = true;
    
    public void setAllowGuestViewCount(boolean allowGuestViewCount) {
        this.allowGuestViewCount = allowGuestViewCount;
    }
    
    public void setEnableIpDuplication(boolean enableIpDuplication) {
        this.enableIpDuplication = enableIpDuplication;
    }
}
