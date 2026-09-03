package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.service.DepartmentService;
import uc.dev.uc_info.service.NoticeService;
import uc.dev.uc_info.service.ScholarshipService;

/**
 * 장학금 관리 컨트롤러. Schedule/Banner/Shuttle과 동일하게 등록/수정은
 * 별도 폼 페이지가 아니라 목록 화면의 모달({@code scholarshipModal})로
 * 처리한다. 목록 화면은 엔티티를 직접 노출하지 않고 {@code ScholarshipDTO}로
 * 변환해서 넘긴다.
 *
 * <p>이 클래스 안에 아래 4개의 요청 핸들러를 직접 작성하세요. 검증 실패 시
 * {@code formError}({@code ValidationMessages.firstError}) 담는 것도
 * 처음부터 포함해서 만들 것(Notice/Schedule/Banner/Shuttle 다 나중에
 * 따로 추가했던 것 — 이번엔 처음부터 넣는다).</p>
 *
 * <h3>만들어야 할 핸들러</h3>
 * <ol>
 *   <li>{@code @GetMapping list(Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)} → {@code "scholarship/scholarship"}
 *       반환. model에 담을 것: {@code scholarships}(DTO 변환된 목록),
 *       {@code totalCount}, {@code departments}(대상 학과 select용,
 *       {@code DepartmentService.findAll()}), {@code linkableNotices}
 *       (연결 공지 select용, {@code noticeService.findAllForLink(admin)}
 *       재사용 — Banner가 이미 만들어둔 것 그대로 씀), {@code scholarshipDTO}
 *       (이미 model에 있으면 새로 안 만듦).</li>
 *   <li>{@code @PostMapping create(...)} — Schedule의 create()와 동일 패턴
 *       (BindingResult 검증, 실패 시 목록 재구성 + openScholarshipModal=true
 *       + formError, 성공 시 createScholarship 호출 후 redirect).</li>
 *   <li>{@code @PostMapping("/{id}/edit") update(...)} — 동일 패턴
 *       (editingScholarshipId 포함).</li>
 *   <li>{@code @PostMapping("/{id}/delete") delete(...)} — 동일 패턴.</li>
 * </ol>
 *
 * <p>목록 DTO 매핑(private {@code toListItems}/{@code toListItem})도
 * Banner/Schedule과 동일한 방식으로 직접 작성할 것 — scholarship의
 * department는 지연로딩이라 deptId만 평탄화하면 되고(연관관계 자체를
 * 안 보여줌), 연결된 notice가 있으면 noticeId만 평탄화한다.</p>
 */
@Controller
@RequestMapping("/scholarships")
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipService scholarshipService;
    private final NoticeService noticeService;
    private final DepartmentService departmentService;
}