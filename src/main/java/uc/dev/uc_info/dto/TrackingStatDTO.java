package uc.dev.uc_info.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공지 하나의 열람 현황 계산 결과. 폼 입력용이 아니라 순수 조회/표시 전용
 * DTO다(검증 애노테이션 없음)
 */
@Getter
@Setter
@NoArgsConstructor
public class TrackingStatDTO {

    /** 공지 PK. */
    private Long noticeId;

    /** 공지 제목. */
    private String title;

    /** 이번 통계가 어떤 학생 집단 기준으로 집계됐는지 표시용 라벨. */
    private String scopeLabel;

    /** 대상 학생 수(scopeLabel 기준으로 좁혀진 인원). */
    private long targetCount;

    /** 열람한 학생 수. */
    private long readCount;

    /** 미확인자 수 (0 미만이면 0으로 방어). */
    private long unreadCount;

    /** 열람률(%). targetCount가 0이면 0.0, 아니면 readCount * 100.0 / targetCount. */
    private double readRate;
}