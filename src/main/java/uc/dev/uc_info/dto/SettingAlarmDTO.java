package uc.dev.uc_info.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 알림 설정 토글 폼 요청 DTO(POST /settings/alarm).
 *
 * <h3>선언해야 할 필드</h3>
 * <ul>
 *   <li>{@code Boolean alarmTracking} — 열람 추적 알림 사용 여부. 체크박스로
 *       받는다. 미전송 시(체크 해제) null로 들어오니 Service에서 false로
 *       취급할 것. 폼 {@code name="alarmTracking"}.</li>
 *   <li>{@code Boolean alarmSend} — 알림 발송 사용 여부. 동일 방식.
 *       폼 {@code name="alarmSend"}.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class SettingAlarmDTO {
}