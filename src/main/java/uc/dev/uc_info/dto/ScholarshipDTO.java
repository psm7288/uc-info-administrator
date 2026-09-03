package uc.dev.uc_info.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장학금 등록/수정 폼 요청 + 목록 화면 표시 겸용 DTO. Notice/Schedule과
 * 같은 패턴 — 자체 department 필드가 있어서 Banner처럼 연결 공지를 통한
 * 간접 권한 판단이 필요 없다.
 *
 * <h3>선언해야 할 필드</h3>
 * <ul>
 *   <li>{@code Long scholarshipId} — 목록 표시 전용(등록 폼에는 없음).</li>
 *   <li>{@code String name} — 장학금명. 필수({@code @NotBlank}). 폼
 *       {@code name="name"}.</li>
 *   <li>{@code String type} — REGIONAL/GRADE/INTERNAL/EXTERNAL. 필수
 *       ({@code @NotBlank}). 폼 {@code name="type"}.</li>
 *   <li>{@code Long deptId} — 대상 학과 PK. 전체 대상이면 null. 검증 없음
 *       (Notice/Schedule의 deptId와 동일 성격). 폼 {@code name="deptId"}.</li>
 *   <li>{@code String targetGrade} — 대상 학년. 전체면 null. "1"/"2"/"3".
 *       폼 {@code name="targetGrade"}.</li>
 *   <li>{@code String residenceCondition} — 거주 조건. 선택(검증 없음).
 *       폼 {@code name="residenceCondition"}.</li>
 *   <li>{@code java.time.LocalDate deadline} — 마감일. 선택. 폼
 *       {@code name="deadline"}(type=date).</li>
 *   <li>{@code Boolean visible} — 노출 여부. 체크박스, 미전송 시 기본
 *       true로 처리(Service에서). 폼 {@code name="visible"}.</li>
 *   <li>{@code Long noticeId} — 연결 공지 PK(참고용, 선택). null이면
 *       연결 안 함. Banner와 달리 권한 판단에는 안 쓰인다. 폼
 *       {@code name="noticeId"}.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class ScholarshipDTO {
}