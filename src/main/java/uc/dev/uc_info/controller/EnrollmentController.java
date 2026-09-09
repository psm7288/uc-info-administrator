package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uc.dev.uc_info.model.Enrollment;
import uc.dev.uc_info.service.EnrollmentService;

import java.util.List;

/**
 * 학생별 수강선택(시간표) 조회 컨트롤러. 학번으로 검색해서 그 학생이
 * 담은 과목 전체를 확인하는 관리자 전용 화면이다. 등록/수정 기능은 없다
 * (조회 전용)
 */
@Controller
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * 학번 검색 화면. studentNumber가 없으면 빈 검색 폼만 보여주고,
     * 있으면 그 학생이 담은 과목 목록을 같이 보여준다
     *
     * @param studentNumber 검색할 학번(선택)
     * @param model         화면 전달용 모델
     * @return "enrollment/enrollment"
     */
    @GetMapping
    public String search(@RequestParam(required = false) String studentNumber,
                         Model model) {
        model.addAttribute("studentNumber", studentNumber);

        if (studentNumber != null && !studentNumber.isBlank()) {
            List<Enrollment> enrollments = enrollmentService.getScheduleForAdmin(studentNumber);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("searched", true);
        }

        return "enrollment/enrollment";
    }
}