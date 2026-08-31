package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.repository.BannerRepository;

/**
 * 배너(Banner) 비즈니스 로직 서비스.
 *
 * <p>이 클래스 안에 아래 7개의 public 메서드(+필요하면 private 헬퍼)를
 * 직접 작성하세요. 컨트롤러는 이 서비스만 호출한다.</p>
 *
 * <h3>가장 중요한 전제 — 배너 권한은 "연결된 공지"를 따른다</h3>
 * <p>{@code Banner} 엔티티엔 자체 department 필드가 없다. 그래서
 * {@code common.validation.DepartmentScoped}를 구현하지 않는다. 대신
 * 권한 판단은 이렇게 한다.</p>
 * <ul>
 *   <li><b>DEPT_ADMIN</b>: 배너 등록/수정 시 공지 연결이 <b>필수</b>다.
 *       연결 안 하면 {@code IllegalArgumentException}("학과 관리자는 공지를
 *       연결해야 배너를 등록할 수 있습니다." 등). 연결한 공지는 반드시 본인
 *       학과 또는 전체 대상이어야 하는데, {@code Notice}가 이미
 *       {@code DepartmentScoped}를 구현하고 있으므로
 *       {@code adminScopeValidator.validateAssignable(admin,
 *       notice.getDepartment())}를 그대로 재사용하면 된다.</li>
 *   <li><b>SUPER_ADMIN</b>: 공지 연결이 <b>선택</b>이다. 연결 안 하면
 *       학교 전체 홍보용 배너로 취급하고 그대로 허용한다.</li>
 *   <li>기존 배너에 대한 접근(조회/수정/삭제) 권한 검사도 같은 원리다:
 *       SUPER_ADMIN은 항상 통과, DEPT_ADMIN은 배너에 연결된 공지가 없으면
 *       접근 자체가 거부되고({@code AccessDeniedException}), 있으면
 *       {@code adminScopeValidator.validateAccess(admin,
 *       banner.getNotice())}로 위임하면 된다.</li>
 *   <li>위 두 규칙(등록 시 검증, 기존 항목 접근 시 검증)은 로직이 비슷하니
 *       private 헬퍼 메서드(예: {@code validateBannerAccess(Admin admin,
 *       Banner banner)})로 뽑아서 update/delete에서 공통으로 쓰는 걸
 *       권장한다.</li>
 * </ul>
 *
 * <h3>Notice 쪽에 추가로 필요한 메서드 — 담당자가 NoticeService에 직접 추가</h3>
 * <p>배너 등록/수정 모달의 "연결 공지" select에 띄울 목록이 필요하다.
 * {@code NoticeService}에 아래 메서드가 없다면 새로 추가해야 한다.
 * (전체 공지를 최신순으로 반환, 상태 무관 — DRAFT도 연결 가능하게 할지는
 * 정책 결정 필요하면 팀에 확인할 것.)</p>
 * <pre>{@code
 * List<Notice> findAllForLink();
 * }</pre>
 *
 * <h3>만들어야 할 메서드</h3>
 * <ol>
 *   <li>{@code List<Banner> findAllFor(Admin admin)} — SUPER_ADMIN이면
 *       {@code bannerRepository.findAllByOrderByCreatedAtDesc()}, DEPT_ADMIN이면
 *       {@code bannerRepository.findByDepartmentOrAll(admin.getDepartment()
 *       .getDeptId())}. admin.getDepartment()가 null이면(데이터 이상)
 *       {@code IllegalStateException}. 둘 다 아닌 role이면
 *       {@code AccessDeniedException}. (Notice/Schedule의 findXxxFor와 동일
 *       패턴 — {@code adminScopeValidator.isSuperAdmin}/{@code isDeptAdmin}
 *       활용.)</li>
 *   <li>{@code Banner getBanner(Long id)} — id null 체크 →
 *       {@code IllegalArgumentException}. {@code findById} 후 없으면
 *       {@code EntityNotFoundException}.</li>
 *   <li>{@code long countAll()} — {@code bannerRepository.count()} 위임.</li>
 *   <li>{@code long countByStatus(String status)} —
 *       {@code bannerRepository.countByStatus(status)} 위임. Controller에서
 *       "ACTIVE"/"SCHEDULED"로 각각 호출해서 activeCount/scheduledCount를
 *       만든다.</li>
 *   <li>{@code List<Notice> findLinkableNotices()} —
 *       {@code noticeService.findAllForLink()} 위임. 등록/수정 모달의 연결
 *       공지 select용.</li>
 *   <li>{@code Banner createBanner(BannerDTO dto, Admin admin)} —
 *       {@code dto.getNoticeId()}가 있으면 {@code noticeService.getNotice
 *       (noticeId)}로 조회, 없으면 null. 위 "권한 전제" 규칙대로 검증한 뒤,
 *       새 {@code Banner}를 만들어 admin/notice/title/subtitle/status/
 *       startDate/endDate를 채우고 save.</li>
 *   <li>{@code Banner updateBanner(Long id, BannerDTO dto, Admin admin)} —
 *       {@code getBanner(id)}로 조회 → 접근 권한 검사(위 전제 참고) → notice
 *       재연결 여부 포함해서 필드 갱신 → save.</li>
 *   <li>{@code void deleteBanner(Long id, Admin admin)} — {@code getBanner(id)}
 *       조회 → 접근 권한 검사 → {@code bannerRepository.delete(banner)}
 *       물리 삭제.</li>
 * </ol>
 *
 * <h3>이 서비스가 던지는 예외</h3>
 * <ul>
 *   <li>{@code EntityNotFoundException} — 없는 배너/공지 id.</li>
 *   <li>{@code IllegalArgumentException} — DEPT_ADMIN이 공지 연결 없이
 *       등록을 시도하는 경우 등.</li>
 *   <li>{@code IllegalStateException} — DEPT_ADMIN인데 소속 학과가 없음
 *       (데이터 이상).</li>
 *   <li>{@code AccessDeniedException} — 다른 학과 공지를 연결하려는 시도,
 *       공지 없는 배너에 DEPT_ADMIN이 접근하려는 시도, 권한 자체가 없는
 *       role.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final NoticeService noticeService;
    private final AdminScopeValidator adminScopeValidator;
}