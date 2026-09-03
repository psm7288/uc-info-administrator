package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.common.validation.DepartmentResolver;
import uc.dev.uc_info.repository.ScholarshipRepository;

/**
 * 장학금(Scholarship) 비즈니스 로직 서비스. Notice/Schedule과 완전히 같은
 * 패턴 — 자체 department 필드가 있어서 {@link AdminScopeValidator}/
 * {@link DepartmentResolver}를 그대로 재사용한다(Banner처럼 연결 공지를
 * 거칠 필요 없음).
 *
 * <p>이 클래스 안에 아래 6개의 public 메서드를 직접 작성하세요.</p>
 *
 * <h3>만들어야 할 메서드</h3>
 * <ol>
 *   <li>{@code List<Scholarship> findScholarshipsFor(Admin admin)}
 *       — SUPER면 {@code findAllByOrderByCreatedAtDesc()}, DEPT면
 *       {@code admin.getDepartment()==null}일 때 {@code IllegalStateException},
 *       아니면 {@code findByDepartmentOrAll(deptId)}. 둘 다 아니면
 *       {@code AccessDeniedException}. (Schedule의 findSchedulesFor와
 *       동일 패턴.)</li>
 *   <li>{@code Scholarship getScholarship(Long id)}
 *       — id null이면 {@code IllegalArgumentException}. 없으면
 *       {@code EntityNotFoundException}.</li>
 *   <li>{@code long countAll(Admin admin)}
 *       — findScholarshipsFor와 같은 권한 범위로 개수 반환(SUPER는
 *       {@code count()}, DEPT는 {@code countByDepartmentOrAll(deptId)}).</li>
 *   <li>{@code Scholarship createScholarship(ScholarshipDTO dto, Admin admin)}
 *       — {@code deptId}를 {@code departmentResolver.resolve(...)}로 변환 →
 *       {@code adminScopeValidator.validateAssignable(admin, department)} →
 *       새 Scholarship 생성, admin/department/name/type/targetGrade
 *       ({@code TextNormalizer.emptyToNull} 적용)/residenceCondition/
 *       deadline/visible(null이면 true) 채우고 save. {@code noticeId}가
 *       있으면 {@code noticeService.getNotice(noticeId)}로 조회해서
 *       연결(없으면 그냥 null로 둠 — 권한 검증 없음, Banner와 다른 점).</li>
 *   <li>{@code Scholarship updateScholarship(Long id, ScholarshipDTO dto, Admin admin)}
 *       — 조회 → {@code adminScopeValidator.validateAccess(admin, scholarship)} →
 *       나머지는 createScholarship과 동일한 필드 갱신 로직.</li>
 *   <li>{@code void deleteScholarship(Long id, Admin admin)}
 *       — 조회 → validateAccess → 물리 삭제.</li>
 * </ol>
 *
 * <p>연결 공지(noticeId) 조회를 위해 {@link NoticeService}도 주입이
 * 필요하다(생성자에 필드 추가).</p>
 *
 * <h3>이 서비스가 던지는 예외</h3>
 * <ul>
 *   <li>{@code EntityNotFoundException} — 없는 장학금/공지 id.</li>
 *   <li>{@code IllegalArgumentException} — id가 null인 경우 등.</li>
 *   <li>{@code IllegalStateException} — DEPT_ADMIN인데 소속 학과 없음.</li>
 *   <li>{@code AccessDeniedException} — 다른 학과 항목 접근/등록 시도, 권한 없는 role.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final AdminScopeValidator adminScopeValidator;
    private final DepartmentResolver departmentResolver;
}