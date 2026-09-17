package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.CourseOfferingService;

/**
 * 개설과목 관리 컨트롤러. DEPT_ADMIN은 본인 학과, SUPER_ADMIN은 전체 학과
 * 개설과목을 CSV로 일괄 등록한다.
 */
@Controller
@RequestMapping("/course-offerings")
@RequiredArgsConstructor
public class CourseOfferingController {

    private final CourseOfferingService courseOfferingService;

    /**
     * 개설과목 관리 화면. 권한 범위 내 목록 + CSV 업로드 폼.
     *
     * @param model     화면 전달용 모델
     * @param principal 로그인 관리자 정보
     * @return "courseOffering/courseOffering"
     */
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();

        model.addAttribute("offerings", courseOfferingService.findOfferingsFor(admin));
        model.addAttribute("totalCount", courseOfferingService.countAll(admin));

        return "courseOffering/courseOffering";
    }

    /**
     * CSV 파일을 업로드해서 개설과목을 일괄 등록한다. Service가 던지는
     * 예외(형식 오류, 다른 학과 과목 업로드 시도 등)는 여기서 안 잡고
     * 전역 예외 처리로 넘긴다.
     *
     * @param file               업로드된 CSV 파일
     * @param principal          로그인 관리자 정보
     * @param redirectAttributes 리다이렉트 후 결과 메시지 전달용
     * @return "redirect:/course-offerings"
     */
    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        Admin admin = principal.getAdmin();
        int count = courseOfferingService.uploadCsv(file, admin);

        redirectAttributes.addFlashAttribute(
                "uploadMessage", count + "건의 개설과목이 등록되었습니다."
        );

        return "redirect:/course-offerings";
    }
}