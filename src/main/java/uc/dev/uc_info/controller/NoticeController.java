package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.dto.NoticeDTO;
import uc.dev.uc_info.service.NoticeService;

/**
 * 공지 관리 컨트롤러.
 *
 * <p>화면 ↔ 핸들러</p>
 * <ul>
 *   <li>GET  /notices              : 목록(+검색) → notice/notice</li>
 *   <li>GET  /notices/new          : 작성 폼   → notice/notice-write</li>
 *   <li>POST /notices              : 작성 처리 → redirect:/notices</li>
 *   <li>GET  /notices/{id}/edit    : 수정 폼   → notice/notice-edit</li>
 *   <li>POST /notices/{id}/edit    : 수정 처리 → redirect:/notices</li>
 *   <li>POST /notices/{id}/approve : 승인      → redirect:/dashboard</li>
 *   <li>POST /notices/{id}/delete  : 삭제      → redirect:/notices</li>
 * </ul>
 *
 * <p>컨트롤러는 NoticeService 만 호출한다(Repository 직접 호출 금지).
 * 로그인 관리자(admin)는 @AuthenticationPrincipal CustomUserPrincipal 에서
 * getAdmin() 으로 꺼낸다(파라미터로 받기).</p>
 *
 * <p>작성/수정 폼에 필요한 학과 목록(departments)은 권한에 맞게 주입해야 한다:
 * SUPER_ADMIN → 전체 학과, DEPT_ADMIN → 본인 학과만. (DepartmentService 사용)</p>
 *
 * <h3>구현 담당자가 알아야 할 예외/검증 처리</h3>
 * <ul>
 *   <li><b>BindingResult 필수</b>: @Valid 를 쓰는 create/update 는 검증 결과를 받을
 *       BindingResult 파라미터를 <b>DTO 바로 뒤에</b> 둬야 한다. 없으면 검증 위반 시
 *       MethodArgumentNotValidException 이 던져져 400 에러가 난다(폼으로 못 돌아감).</li>
 *   <li><b>검증 실패 처리</b>: result.hasErrors() 면 departments 를 다시 model 에 담고
 *       작성/수정 폼 뷰를 그대로 반환해 에러 메시지를 보여준다(리다이렉트 X).</li>
 *   <li><b>서비스 예외는 전역에서</b>: 서비스가 던지는 EntityNotFoundException /
 *       IllegalStateException / 권한 예외(AccessDenied 등)는 여기서 try-catch 하지 말고
 *       전역 예외 처리(@ControllerAdvice, 본인 담당)로 흘려보낸다.</li>
 *   <li>로그인 admin 획득: @AuthenticationPrincipal CustomUserPrincipal principal →
 *       principal.getAdmin(). principal 이 null 인 경우는 Security 가 이미 차단(인증 필요).</li>
 * </ul>
 */
@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    // TODO(담당): 학과 목록 주입용 DepartmentService 도 주입.

    /**
     * 공지 목록 화면.
     *
     * <p>처리: 로그인 admin 의 권한에 맞는 목록을 서비스로 조회해 화면에 전달한다.</p>
     * <p>model 에 담을 것: 공지 목록(notices), 전체 건수(totalCount),
     * 게시중 건수(publishedCount). — notice/notice.html 이 사용하는 속성명에 맞춰 담기.</p>
     * <p>예외: 단순 조회라 별도 throw 없음. admin 은 Security 가 보장(인증된 요청).</p>
     *
     * @return "notice/notice"
     */
    @GetMapping
    public String list(Model model) {
        throw new UnsupportedOperationException(
                "구현 예정: 권한별 목록 조회 + 통계(notices/totalCount/publishedCount) model 주입");
    }

    /**
     * 작성 폼 화면.
     *
     * <p>처리: 빈 입력 DTO 와, 권한에 맞는 학과 목록을 화면에 전달한다.</p>
     * <p>model 에 담을 것: 빈 NoticeDTO(noticeDTO), 대상 학과 목록(departments).
     * — notice-write.html 의 대상 학과 select 가 ${departments} 를 사용.</p>
     *
     * @return "notice/notice-write"
     */
    @GetMapping("/new")
    public String writeForm(Model model) {
        throw new UnsupportedOperationException(
                "구현 예정: 빈 noticeDTO + 권한별 departments model 주입");
    }

    /**
     * 작성 처리.
     *
     * <p>처리: 로그인 admin 으로 서비스 createNotice 호출 → 목록으로 리다이렉트(PRG).
     * 작성 폼 버튼 name=status(DRAFT/PUBLISHED).</p>
     * <p>예외/검증(담당자 구현 시):
     *   <b>BindingResult 를 dto 바로 뒤에 추가</b>하고, hasErrors() 면 departments 다시 담아
     *   "notice/notice-write" 반환. 서비스의 권한·검증 예외는 전역 처리로 보냄.</p>
     *
     * @param dto 작성·검증 DTO(@Valid @ModelAttribute)
     * @return "redirect:/notices"
     */
    @PostMapping
    public String create(@Valid @ModelAttribute NoticeDTO dto) {
        // 구현 시 시그니처 예: create(@Valid @ModelAttribute NoticeDTO dto,
        //                            BindingResult result,
        //                            @AuthenticationPrincipal CustomUserPrincipal principal, Model model)
        throw new UnsupportedOperationException(
                "구현 예정: BindingResult 검증분기 → admin 획득 → createNotice(dto, admin) → redirect:/notices");
    }

    /**
     * 수정 폼 화면.
     *
     * <p>처리: id 로 기존 공지를 조회하고, 권한에 맞는 학과 목록과 함께 화면에 전달한다.</p>
     * <p>model 에 담을 것: 기존 공지(notice), 대상 학과 목록(departments).
     * — notice-edit.html 이 ${notice}, ${departments} 사용.</p>
     * <p>예외: 서비스 getNotice(id) 가 없는 id 면 EntityNotFoundException 을 던진다 →
     *   여기서 잡지 말고 전역 처리(@ControllerAdvice)로.</p>
     *
     * @param id 공지 PK
     * @return "notice/notice-edit"
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        throw new UnsupportedOperationException(
                "구현 예정: getNotice(id) → notice + departments model 주입");
    }

    /**
     * 수정 처리.
     *
     * <p>처리: 로그인 admin 으로 서비스 updateNotice 호출 → 목록으로 리다이렉트.
     * 수정 폼 버튼 name=saveType(DRAFT/PUBLISHED).</p>
     * <p>예외/검증(담당자 구현 시): create 와 동일하게 <b>BindingResult</b> 추가 + hasErrors 분기
     *   (실패 시 notice + departments 다시 담아 "notice/notice-edit" 반환).
     *   서비스의 EntityNotFound/권한 예외는 전역 처리로.</p>
     *
     * @param id  공지 PK
     * @param dto 수정 DTO(@Valid @ModelAttribute)
     * @return "redirect:/notices"
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute NoticeDTO dto) {
        throw new UnsupportedOperationException(
                "구현 예정: BindingResult 검증분기 → admin 획득 → updateNotice(id, dto, admin) → redirect:/notices");
    }

    /**
     * 공지 승인(WAITING → PUBLISHED).
     *
     * <p>처리: 서비스 approveNotice 호출 후 대시보드로 리다이렉트.</p>
     * <p>예외: 서비스가 없는 id → EntityNotFoundException, WAITING 아님 →
     *   IllegalStateException 을 던진다. 여기서 잡지 말고 전역 처리로.</p>
     *
     * @param id 공지 PK
     * @return "redirect:/dashboard"
     */
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        throw new UnsupportedOperationException(
                "구현 예정: admin 획득 → approveNotice(id, admin) → redirect:/dashboard");
    }

    /**
     * 공지 삭제.
     *
     * <p>처리: 서비스 deleteNotice 호출 후 목록으로 리다이렉트.</p>
     * <p>예외: 서비스가 없는 id → EntityNotFoundException, 권한 위반 → 권한 예외.
     *   전역 처리로 흘려보낸다.</p>
     *
     * @param id 공지 PK
     * @return "redirect:/notices"
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        throw new UnsupportedOperationException(
                "구현 예정: admin 획득 → deleteNotice(id, admin) → redirect:/notices");
    }
}