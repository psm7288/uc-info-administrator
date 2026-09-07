package uc.dev.uc_info.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 알림 설정 변경 요청 DTO.
 *
 * <p>알림 설정 화면에서 전달된 각 알림의 활성화 여부를 전달한다.
 * 체크되지 않은 항목은 {@code null}로 전달될 수 있으며,
 * 서비스에서 {@code false}로 처리한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class SettingAlarmDTO {

    /**
     * 열람 추적 알림 사용 여부.
     */
    private Boolean alarmTracking;

    /**
     * 알림 발송 사용 여부.
     */
    private Boolean alarmSend;
}