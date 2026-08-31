package uc.dev.uc_info.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 배너 등록/수정 폼 요청 + 목록 화면 표시 겸용 DTO.
 *
 * <p>이 클래스 안에 아래 필드들을 직접 선언하세요. Banner는 자체 department
 * 필드가 없으므로, 학과 select 대신 <b>연결 공지 select</b>(noticeId)가
 * 그 역할을 대신한다.</p>
 *
 * <h3>선언해야 할 필드</h3>
 * <ul>
 *   <li>{@code String title} — 배너 제목. 필수({@code @NotBlank}). 폼
 *       {@code name="title"}.</li>
 *   <li>{@code String subtitle} — 부제/날짜 텍스트. 선택(검증 없음). 폼
 *       {@code name="subtitle"}.</li>
 *   <li>{@code String status} — 노출 상태: ACTIVE/SCHEDULED/INACTIVE. 필수
 *       ({@code @NotBlank}). 폼 {@code name="status"}.</li>
 *   <li>{@code java.time.LocalDate startDate} — 노출 시작일. 선택. 폼
 *       {@code name="startDate"}(type=date).</li>
 *   <li>{@code java.time.LocalDate endDate} — 노출 종료일. 선택. 폼
 *       {@code name="endDate"}(type=date).</li>
 *   <li>{@code Long noticeId} — 연결할 공지 PK. DEPT_ADMIN은 사실상 필수
 *       (Service가 검증), SUPER_ADMIN은 선택 — null이면 "연결 안 함". 폼의
 *       연결 공지 select 옵션 value. 검증 애노테이션은 없음(필수 여부가
 *       role에 따라 달라져서 DTO 레벨이 아니라 Service 레벨에서 검증).</li>
 *   <li>{@code Long bannerId} — 배너 PK. <b>목록 표시 전용</b>(등록 폼에는
 *       없음). 검증 없음.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class BannerDTO {
}