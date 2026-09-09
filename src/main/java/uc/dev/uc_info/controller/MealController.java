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
import uc.dev.uc_info.service.MealService;

/**
 * 식단 관리 컨트롤러.
 *
 * <p>등록/수정은 폼이 아니라 CSV 파일 업로드로 처리한다 — 매일 세끼를
 * 하나씩 타이핑하는 대신, 미리 준비한 CSV를 통째로 올린다.</p>
 */
@Controller
@RequestMapping("/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    /**
     * 식단 관리 화면. 최근 업로드된 식단 목록 + CSV 업로드 폼을 보여준다.
     *
     * @param model 화면 전달용 모델
     * @return "meal/meal"
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("meals", mealService.findRecent());
        return "meal/meal";
    }

    /**
     * CSV 파일을 업로드해서 식단을 일괄 등록/갱신한다.
     *
     * @param file               업로드된 CSV 파일
     * @param principal          로그인 관리자 정보
     * @param redirectAttributes 리다이렉트 후 결과 메시지 전달용
     * @return "redirect:/meals"
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @AuthenticationPrincipal CustomUserPrincipal principal,
                         RedirectAttributes redirectAttributes) {
        Admin admin = principal.getAdmin();
        int count = mealService.uploadCsv(file, admin);
        redirectAttributes.addFlashAttribute(
                "uploadMessage", count + "건의 식단이 등록/갱신되었습니다."
        );

        return "redirect:/meals";
    }
}