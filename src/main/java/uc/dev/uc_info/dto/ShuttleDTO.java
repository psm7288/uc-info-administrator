package uc.dev.uc_info.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 셔틀버스 노선 등록/수정 폼 요청 + 목록 화면 표시 겸용 DTO.
 *
 * <p>이 클래스 안에 아래 필드들을 직접 선언하세요. {@code NoticeDTO}/
 * {@code ScheduleDTO}와 같은 패턴 — 폼 입력용 검증 필드와 목록 표시용
 * 필드를 한 클래스에 같이 둔다(별도 ResponseDTO는 만들지 않는다).</p>
 *
 * <h3>선언해야 할 필드</h3>
 * <ul>
 *   <li>{@code String routeName} — 노선명. 필수({@code @NotBlank}). 폼
 *       {@code name="routeName"}.</li>
 *   <li>{@code String departure} — 출발지. 필수({@code @NotBlank}). 폼
 *       {@code name="departure"}.</li>
 *   <li>{@code String destination} — 도착지. 필수({@code @NotBlank}). 폼
 *       {@code name="destination"}.</li>
 *   <li>{@code String waypoints} — 경유지 설명. 선택(검증 없음). 폼
 *       {@code name="waypoints"}.</li>
 *   <li>{@code String firstDeparture} — 첫차 시간. "08:00" 같은 문자열,
 *       {@code @Size(max = 10)}. 폼 {@code name="firstDeparture"}.</li>
 *   <li>{@code String lastDeparture} — 막차 시간. 동일 형식,
 *       {@code @Size(max = 10)}. 폼 {@code name="lastDeparture"}.</li>
 *   <li>{@code String status} — 운행 상태: ACTIVE/SUSPENDED/DELAYED. 필수
 *       ({@code @NotBlank}). 폼 {@code name="status"}.</li>
 *   <li>{@code Long shuttleId} — 노선 PK. <b>목록 표시 전용</b>(등록 폼에는
 *       없음, 수정/삭제 버튼의 {@code data-id}로 사용). 검증 없음.</li>
 * </ul>
 *
 * <p>{@code @Getter}/{@code @Setter}/{@code @NoArgsConstructor}는 이미
 * 붙어있으니 필드만 선언하면 자동으로 getter/setter가 생긴다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ShuttleDTO {
}