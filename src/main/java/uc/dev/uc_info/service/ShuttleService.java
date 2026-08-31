package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.dev.uc_info.repository.ShuttleRepository;

/**
 * 셔틀버스 노선(Shuttle) 비즈니스 로직 서비스.
 *
 * <p>이 클래스 안에 아래 6개의 public 메서드를 직접 작성하세요. 컨트롤러는
 * 이 서비스만 호출한다(Repository 직접 호출 금지). 조회 메서드에는
 * {@code @Transactional(readOnly = true)}, 등록/수정/삭제 메서드에는
 * {@code @Transactional}을 붙인다.</p>
 *
 * <h3>가장 중요한 전제 — Notice/Schedule과 다른 점</h3>
 * <p>셔틀은 SUPER_ADMIN 전용 기능이고, {@code SecurityConfig}에서 이미
 * {@code /shuttles/**}를 {@code hasRole("SUPER_ADMIN")}으로 URL 단에서
 * 막아놨다. 즉 이 서비스 코드에 도달한 시점엔 이미 SUPER_ADMIN이 보장된다.
 * 그래서 Notice/Schedule에 있던 {@code common.validation.AdminScopeValidator}
 * (DEPT_ADMIN 학과 분기 검증)와 {@code DepartmentResolver}(deptId→Department
 * 변환)는 이 클래스에서 전혀 쓰지 않는다 — 학과 개념 자체가 없는 도메인이다.
 * {@code AccessDeniedException}을 던질 이유도 없다.</p>
 *
 * <h3>만들어야 할 메서드</h3>
 * <ol>
 *   <li>{@code List<Shuttle> findAll()}
 *       — {@code shuttleRepository.findAllByOrderByCreatedAtDesc()} 그대로
 *       위임한다. 권한 분기 없음(SUPER_ADMIN만 도달하므로 전체 목록 하나면
 *       충분하다).</li>
 *   <li>{@code Shuttle getShuttle(Long id)}
 *       — id가 null이면 {@code IllegalArgumentException}. {@code findById(id)}
 *       호출 후 없으면 {@code EntityNotFoundException}("셔틀 노선을 찾을 수
 *       없습니다." 등 메시지).</li>
 *   <li>{@code long countAll()} — {@code shuttleRepository.count()} 위임.</li>
 *   <li>{@code long countActive()}
 *       — {@code shuttleRepository.countByStatus("ACTIVE")} 위임. 화면 상단
 *       "운영 노선 수" 통계용.</li>
 *   <li>{@code Shuttle createShuttle(ShuttleDTO dto, Admin admin)}
 *       — routeName/departure/destination 공백 검증은 DTO의
 *       {@code @NotBlank}와 컨트롤러의 BindingResult가 이미 처리했다고 보고
 *       여기서 재검사하지 않는다. 새 {@code Shuttle} 객체를 만들어
 *       admin(등록자, NOT NULL)/routeName/departure/destination/waypoints/
 *       firstDeparture/lastDeparture/status를 dto 값으로 채우고 save 후
 *       반환한다. 학과 관련 처리가 전혀 없다는 점이 Notice/Schedule의
 *       createXxx와 가장 다른 부분이다.</li>
 *   <li>{@code Shuttle updateShuttle(Long id, ShuttleDTO dto, Admin admin)}
 *       — {@code getShuttle(id)}로 조회 후 routeName/departure/destination/
 *       waypoints/firstDeparture/lastDeparture/status를 dto 값으로 갱신하고
 *       save. 등록자(admin) 필드는 수정 시 변경하지 않는다.</li>
 *   <li>{@code void deleteShuttle(Long id, Admin admin)}
 *       — {@code getShuttle(id)}로 조회 후 물리 삭제
 *       ({@code shuttleRepository.delete(shuttle)}). 다른 엔티티가 FK로
 *       참조하지 않는 구조라 소프트 삭제는 불필요하다. "운행중단"은 삭제가
 *       아니라 {@code updateShuttle}로 status를 "SUSPENDED"로 바꾸는 것으로
 *       처리하므로 별도 메서드가 필요 없다.</li>
 * </ol>
 *
 * <h3>이 서비스가 던지는 예외 (전역 GlobalExceptionAdvice가 받는다)</h3>
 * <ul>
 *   <li>{@code EntityNotFoundException} — id로 조회했는데 없을 때.</li>
 *   <li>{@code IllegalArgumentException} — id가 null인 경우 등.</li>
 * </ul>
 * <p>{@code AccessDeniedException}은 던질 필요 없다(위 전제 참고).</p>
 */
@Service
@RequiredArgsConstructor
public class ShuttleService {

    private final ShuttleRepository shuttleRepository;
}