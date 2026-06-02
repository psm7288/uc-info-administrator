package uc.dev.uc_info.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uc.dev.uc_info.dto.NoticeDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Notice;
import uc.dev.uc_info.repository.NoticeRepository;

import java.util.List;

/**
 * 공지(Notice) 비즈니스 로직 서비스.
 *
 * <p>컨트롤러는 이 서비스만 호출하고, Repository 를 직접 부르지 않는다(MVC 계층 분리).
 * 트랜잭션은 이 레이어에서 건다 — 조회는 readOnly, 변경은 일반 트랜잭션.</p>
 *
 * <p>권한(권한 매트릭스): DEPT_ADMIN 은 본인 학과 + 전체 대상 공지만,
 * SUPER_ADMIN 은 모든 공지를 다룬다. 범위 판단을 메서드별로 명세했다.</p>
 *
 * <p>NOTE: 지금은 단일 서비스. 변경/통계/파일 로직이 커지면 NoticeDBService 로 분리.</p>
 *
 * <h3>구현 담당자가 던져야 할 예외 (전역 @ControllerAdvice 에서 받는다)</h3>
 * <ul>
 *   <li><b>EntityNotFoundException</b>: id 로 조회했는데 없을 때 (getNotice/update/approve/delete).</li>
 *   <li><b>IllegalArgumentException</b>: 입력값이 잘못됐을 때
 *       (title 공백, endDate &lt; startDate, deptId 가 존재하지 않는 학과 등).</li>
 *   <li><b>IllegalStateException</b>: 상태 전이가 규칙 위반일 때 (승인인데 WAITING 이 아님 등).</li>
 *   <li><b>권한 예외</b>: DEPT_ADMIN 이 남의 학과 대상에 접근/작성 시
 *       (org.springframework.security.access.AccessDeniedException 또는 IllegalStateException).</li>
 * </ul>
 * <p>예외를 던지기만 하면 되고, 화면/응답 변환은 전역 처리(본인 담당)가 맡는다.</p>
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
// TODO(담당): 대상 학과 변환(dto.deptId → Department)을 위해 DepartmentRepository 주입.

    // ============================================================
    // 조회
    // ============================================================

    /**
     * 공지 목록을 조회한다. — 공지 관리 화면(/notices).
     *
     * <p>권한 분기</p>
     * <ul>
     *   <li>SUPER_ADMIN → 전체 공지(findAllByOrderByCreatedAtDesc)</li>
     *   <li>DEPT_ADMIN  → 본인 학과 + 전체 대상
     *       (findByDepartmentOrAll(admin.getDepartment().getDeptId()))</li>
     * </ul>
     * <p>예외: 없음(목록은 비어도 정상). 단 DEPT_ADMIN 인데 department 가 null 이면
     *   데이터 이상 → IllegalStateException 으로 방어 가능.</p>
     *
     * @param admin 로그인한 관리자(권한·소속 학과 판단용)
     * @return 권한 범위에 맞는 공지 목록(최신순)
     */
    @Transactional(readOnly = true)
    public List<Notice> findNoticesFor(Admin admin) {
        throw new UnsupportedOperationException(
                "구현 예정: admin.role 로 분기 → SUPER_ADMIN 전체 / DEPT_ADMIN 본인학과+전체");
    }

    /**
     * 단건 공지를 조회한다. — 수정 폼 진입(/notices/{id}/edit) 시.
     *
     * <p>예외: 없는 id → <b>EntityNotFoundException</b>
     *   (findById(id).orElseThrow(() -&gt; new EntityNotFoundException(...))).</p>
     *
     * @param id 공지 PK
     * @return 공지 엔티티
     */
    @Transactional(readOnly = true)
    public Notice getNotice(Long id) {
        throw new UnsupportedOperationException("구현 예정: findById + orElseThrow(EntityNotFoundException)");
    }

    /**
     * 상태별 공지 개수. — 대시보드/목록 통계(예: 게시중 몇 건).
     *
     * @param status 공지 상태
     * @return 개수 (countByStatus 위임). 예외 없음.
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        throw new UnsupportedOperationException("구현 예정: countByStatus 위임");
    }

    /**
     * 전체 공지 개수. — 목록 상단 'totalCount'.
     *
     * @return 전체 건수 (count 위임). 예외 없음.
     */
    @Transactional(readOnly = true)
    public long countAll() {
        throw new UnsupportedOperationException("구현 예정: count 위임");
    }

    // ============================================================
    // (작성/수정/승인/삭제)
    // ============================================================

    /**
     * 새 공지를 작성한다. — 작성 폼 제출(POST /notices).
     *
     * <p>처리 흐름</p>
     * <ol>
     *   <li>새 Notice 만들고 dto 값 채우기(title, content, category, priority,
     *       targetGrade, startDate, endDate).</li>
     *   <li>작성자 연결: notice.setAdmin(admin). (admin 은 nullable=false 라 필수)</li>
     *   <li>대상 학과 설정: dto.deptId 가 있으면 그 Department 를 찾아 set,
     *       비어있으면(전체 대상) null. — DepartmentRepository 로 조회.</li>
     *   <li>초기 상태: dto.status 가 "PUBLISHED" 면 PUBLISHED, 아니면 DRAFT.</li>
     *   <li>save.</li>
     * </ol>
     *
     * <p>여기서 던질 예외(담당자 구현):</p>
     * <ul>
     *   <li>DEPT_ADMIN 이 dto.deptId 를 본인 학과/전체(null) 외로 지정 → <b>권한 예외</b>.</li>
     *   <li>dto.deptId 가 존재하지 않는 학과 → <b>EntityNotFoundException</b> 또는 IllegalArgumentException.</li>
     *   <li>endDate &lt; startDate (둘 다 있을 때) → <b>IllegalArgumentException</b>.</li>
     * </ul>
     *
     * @param dto   작성·검증 DTO
     * @param admin 작성자(로그인 관리자)
     * @return 저장된 공지
     */
    @Transactional
    public Notice createNotice(NoticeDTO dto, Admin admin) {
        throw new UnsupportedOperationException(
                "구현 예정: dto→Notice 매핑, admin/department 연결, 권한·기간 검증, save");
    }

    /**
     * 기존 공지를 수정한다. — 수정 폼 제출(POST /notices/{id}/edit).
     *
     * <p>처리 흐름</p>
     * <ol>
     *   <li>id 로 기존 공지 조회(없으면 <b>EntityNotFoundException</b>).</li>
     *   <li>권한 확인: DEPT_ADMIN 이면 이 공지가 본인 학과/전체 대상인지 검사.
     *       아니면 <b>권한 예외</b>.</li>
     *   <li>필드 갱신: title/content/category/priority/targetGrade/startDate/endDate,
     *       대상 학과(dto.deptId → Department or null).</li>
     *   <li>상태 전이: dto.saveType("PUBLISHED"/"DRAFT") 기준으로 status 변경.</li>
     *   <li>updatedAt 은 Auditing 자동(변경 감지 저장).</li>
     * </ol>
     * <p>기간 역전(endDate &lt; startDate) → IllegalArgumentException.</p>
     *
     * @param id    수정할 공지 PK
     * @param dto   수정 DTO(버튼 값은 saveType)
     * @param admin 수정 요청 관리자(권한 판단)
     * @return 수정된 공지
     */
    @Transactional
    public Notice updateNotice(Long id, NoticeDTO dto, Admin admin) {
        throw new UnsupportedOperationException(
                "구현 예정: 조회(EntityNotFound)→권한확인→필드 갱신→saveType 상태전이");
    }

    /**
     * 검토중(WAITING) 공지를 게시(PUBLISHED)로 승인한다. — 승인 버튼.
     *
     * <p>처리 흐름: id 로 조회(없으면 <b>EntityNotFoundException</b>) →
     *   현재 status 가 WAITING 인지 확인(아니면 <b>IllegalStateException</b>) →
     *   PUBLISHED 로 변경. DEPT_ADMIN 이면 본인 학과 대상인지 권한 확인.</p>
     *
     * @param id    공지 PK
     * @param admin 승인 관리자(권한 판단)
     */
    @Transactional
    public void approveNotice(Long id, Admin admin) {
        throw new UnsupportedOperationException("구현 예정: 조회→권한→WAITING 검증→PUBLISHED 전이");
    }

    /**
     * 공지를 삭제한다. — 삭제 버튼.
     *
     * <p>정책: Banner/NoticeReadLog 가 이 공지를 FK 로 참조할 수 있어 물리 삭제 시
     *   제약 위반 가능. <b>소프트 삭제(status="CLOSED")</b> 권장. 물리 삭제를 택하면
     *   연관 레코드부터 정리.</p>
     * <p>예외: 없는 id → <b>EntityNotFoundException</b>, DEPT_ADMIN 권한 위반 → <b>권한 예외</b>.</p>
     *
     * @param id    공지 PK
     * @param admin 삭제 관리자(권한 판단)
     */
    @Transactional
    public void deleteNotice(Long id, Admin admin) {
        throw new UnsupportedOperationException("구현 예정: 조회→권한확인→소프트삭제(CLOSED) 권장");
    }
}