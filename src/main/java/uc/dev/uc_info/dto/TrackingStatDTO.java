package uc.dev.uc_info.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공지 하나의 열람 현황 계산 결과. 폼 입력용이 아니라 순수 조회/표시 전용
 * DTO다(검증 애노테이션 없음) — {@code TrackingService}가 계산해서 채운다.
 *
 * <h3>선언해야 할 필드</h3>
 * <ul>
 *   <li>{@code Long noticeId} — 공지 PK.</li>
 *   <li>{@code String title} — 공지 제목.</li>
 *   <li>{@code long targetCount} — 대상 학생 수({@code UserRepository
 *       .countTargetStudents} 결과).</li>
 *   <li>{@code long readCount} — 열람한 학생 수({@code NoticeReadLogRepository
 *       .countByNotice_NoticeId} 결과).</li>
 *   <li>{@code long unreadCount} — 미확인자 수. {@code targetCount - readCount}
 *       (음수 방지: targetCount보다 readCount가 클 수 없다는 전제이지만,
 *       혹시 모르니 0 미만이면 0으로 방어할 것).</li>
 *   <li>{@code double readRate} — 열람률(%). {@code targetCount}가 0이면
 *       0.0(0으로 나누기 방지), 아니면 {@code readCount * 100.0 / targetCount}.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class TrackingStatDTO {
}