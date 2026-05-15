package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScholarshipController {

    @GetMapping("/scholarships")
    public String scholarshipPage() {
        return "scholarship";
    }
}