package uc.dev.uc_info.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uc.dev.uc_info.repository.ShuttleRepository;

@Controller
@RequestMapping("/shuttles")
@RequiredArgsConstructor
public class ShuttleController {

    private final ShuttleRepository shuttleRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("shuttles", shuttleRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("activeCount", shuttleRepository.countByStatus("ACTIVE"));
        return "shuttle/shuttle";
    }
}