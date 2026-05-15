package uc.dev.uc_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrackingController {

    @GetMapping("/tracking")
    public String trackingPage() {
        return "tracking";
    }
}