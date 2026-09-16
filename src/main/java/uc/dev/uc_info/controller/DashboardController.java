package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.common.validation.AdminScopeValidator;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.repository.UserRepository;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.BannerService;
import uc.dev.uc_info.service.NoticeService;

import java.util.List;

/**
 * 관리자 대시보드 화면을 담당하는 컨트롤러.
 *
 * <p>로그인 성공 후 이동하는 /dashboard 요청을 처리하고, 상단 통계
 * 카드와 최근 공지 목록을 권한 범위(admin.department)에 맞춰 채운다.</p>
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /** "최근 공지" 패널에 보여줄 최대 건수. */
    private static final int RECENT_NOTICE_LIMIT = 5;

    private final NoticeService noticeService;
    private final BannerService bannerService;
    private final UserRepository userRepository;
    private final AdminScopeValidator adminScopeValidator;

    /**
     * 관리자 대시보드 화면을 반환한다.
     *
     * <p>게시 중 공지 수, 활성 배너 수, 대상 학생 수, 최근 공지 목록을
     * 로그인 관리자의 권한 범위(SUPER_ADMIN=전체, DEPT_ADMIN=본인 학과)로
     * 채운다. 공지 승인(WAITING) 기능은 폐지되어 waitingCount/waitingNotices는
     * 항상 0/빈 목록이다 — 화면의 승인 대기 패널은 자연히 숨는다.</p>
     *
     * @param principal 로그인 관리자 정보
     * @param model     화면 전달용 모델
     * @return templates/dashboard/dashboard.html 템플릿 경로
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     */
    @GetMapping
    public String dashboard(@AuthenticationPrincipal CustomUserPrincipal principal, Model model) {
        Admin admin = principal.getAdmin();

        model.addAttribute("publishedCount", noticeService.countByStatus("PUBLISHED", admin));
        model.addAttribute("activeBannerCount", bannerService.countByStatus("ACTIVE", admin));
        model.addAttribute("totalUserCount", countTargetStudents(admin));
        model.addAttribute("recentNotices",
                noticeService.findNoticesFor(admin).stream().limit(RECENT_NOTICE_LIMIT).toList());

        model.addAttribute("waitingCount", 0L);
        model.addAttribute("waitingNotices", List.of());

        return "dashboard/dashboard";
    }

    /**
     * "전체 학생" 카드용 대상 학생 수. SUPER_ADMIN은 전체, DEPT_ADMIN은
     * 본인 학과만 — 다른 통계와 같은 권한 범위로 센다.
     *
     * @param admin 로그인 관리자
     * @return 재학 중이며 앱 접근이 허용된 대상 학생 수
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우
     */
    private long countTargetStudents(Admin admin) {
        if (adminScopeValidator.isSuperAdmin(admin)) {
            return userRepository.countTargetStudents(null, null);
        }

        if (admin.getDepartment() == null) {
            throw new IllegalStateException("학과 관리자의 소속 학과가 없습니다.");
        }
        return userRepository.countTargetStudents(admin.getDepartment().getDeptId(), null);
    }
}