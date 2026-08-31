package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.service.ShuttleService;

/**
 * 셔틀버스 노선 관리 컨트롤러.
 *
 * <p>이 클래스 안에 아래 4개의 요청 핸들러 메서드를 직접 작성하세요.
 * Schedule과 동일하게 등록/수정은 별도 폼 페이지가 아니라 목록 화면의
 * 모달({@code shuttleModal})로 처리한다 — 그래서 작성/수정 '폼 GET'
 * 핸들러는 없고 목록(GET)과 처리(POST) 4개만 있다.</p>
 *
 * <p>컨트롤러는 {@code ShuttleService}만 호출한다(Repository 직접 호출
 * 금지). 로그인 admin은 {@code @AuthenticationPrincipal CustomUserPrincipal
 * principal} 파라미터로 받아 {@code principal.getAdmin()}으로 꺼낸다.
 * Schedule의 {@code DepartmentService}에 대응하는 의존성은 없다 — 대상
 * 학과 select 자체가 이 화면엔 없다.</p>
 *
 * <h3>만들어야 할 핸들러</h3>
 * <ol>
 *   <li>{@code @GetMapping list(Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)} → {@code "shuttle/shuttle"} 반환.
 *       model에 담을 것: {@code shuttles}({@code shuttleService.findAll()}),
 *       {@code totalCount}({@code countAll()}), {@code activeCount}
 *       ({@code countActive()}), {@code shuttleDTO}(모달 바인딩용 — 이미
 *       model에 있으면(검증 실패 후 복귀) 새로 만들지 않음, Schedule의
 *       list()와 동일 패턴).</li>
 *   <li>{@code @PostMapping create(@Valid @ModelAttribute("shuttleDTO")
 *       ShuttleDTO dto, BindingResult bindingResult, Model model,
 *       @AuthenticationPrincipal CustomUserPrincipal principal)}.
 *       {@code bindingResult.hasErrors()}면 목록 model(shuttles/totalCount/
 *       activeCount)을 다시 채우고 {@code openShuttleModal=true}를 추가해서
 *       {@code "shuttle/shuttle"}을 반환(모달 재오픈). 아니면
 *       {@code shuttleService.createShuttle(dto, admin)} 호출 후
 *       {@code "redirect:/shuttles"}.</li>
 *   <li>{@code @PostMapping("/{id}/edit") update(@PathVariable Long id,
 *       @Valid @ModelAttribute("shuttleDTO") ShuttleDTO dto, BindingResult
 *       bindingResult, Model model, @AuthenticationPrincipal
 *       CustomUserPrincipal principal)}. create와 동일하되 검증 실패 시
 *       {@code editingShuttleId}(=id)도 함께 model에 추가해서 "이 노선을
 *       편집 중"인 상태로 모달이 다시 열리게 한다. 성공 시
 *       {@code shuttleService.updateShuttle(id, dto, admin)} 호출.</li>
 *   <li>{@code @PostMapping("/{id}/delete") delete(@PathVariable Long id,
 *       @AuthenticationPrincipal CustomUserPrincipal principal)}.
 *       {@code shuttleService.deleteShuttle(id, admin)} 호출 후
 *       {@code "redirect:/shuttles"}. 별도 확인 화면 없이 바로 삭제한다
 *       (화면의 삭제 확인 모달에서 이미 확인시킨 뒤 호출되는 구조).</li>
 * </ol>
 *
 * <p>Service가 던지는 예외는 여기서 잡지 않는다 — 전역
 * {@code GlobalExceptionAdvice}가 flash+에러모달로 처리한다.</p>
 */
@Controller
@RequestMapping("/shuttles")
@RequiredArgsConstructor
public class ShuttleController {

    private final ShuttleService shuttleService;
}