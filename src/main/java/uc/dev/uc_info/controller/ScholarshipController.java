package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 장학금 관리. ERD 상 scholarship 테이블은 보류(모델만 개발).
 * TODO: Scholarship 엔티티 확정 후 실데이터 연동.
 */
@Controller
@RequestMapping("/scholarships")
public class ScholarshipController {

    @GetMapping
    public String list() {
        return "scholarship/scholarship";
    }
}