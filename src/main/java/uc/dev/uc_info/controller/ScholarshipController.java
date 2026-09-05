package uc.dev.uc_info.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.common.util.ValidationMessages;
import uc.dev.uc_info.dto.ScholarshipDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Scholarship;
import uc.dev.uc_info.security.core.CustomUserPrincipal;
import uc.dev.uc_info.service.DepartmentService;
import uc.dev.uc_info.service.NoticeService;
import uc.dev.uc_info.service.ScholarshipService;

import java.util.List;

/**
 * 장학금 관리 컨트롤러.
 *
 * <p>장학금 목록 조회와 등록, 수정, 삭제 요청을 처리한다.
 * 등록/수정은 별도 페이지가 아닌 scholarshipModal을 사용한다.</p>
 */
@Controller
@RequestMapping("/scholarships")
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipService scholarshipService;
    private final NoticeService noticeService;
    private final DepartmentService departmentService;

    /**
     * 장학금 관리 목록 화면을 조회한다.
     *
     * @param model 화면에 전달할 데이터
     * @param principal 로그인 관리자 정보
     * @return 장학금 관리 화면
     */
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal CustomUserPrincipal principal) {
        Admin admin = principal.getAdmin();
        setListModel(model, admin);

        if (!model.containsAttribute("scholarshipDTO")) {
            model.addAttribute("scholarshipDTO", new ScholarshipDTO());
        }

        return "scholarship/scholarship";
    }

    /**
     * 새 장학금을 등록한다.
     *
     * @param dto 장학금 등록 정보
     * @param bindingResult 검증 결과
     * @param model 화면에 전달할 데이터
     * @param principal 로그인 관리자 정보
     * @return 장학금 목록 화면 또는 리다이렉트
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("scholarshipDTO") ScholarshipDTO dto,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Admin admin = principal.getAdmin();

        if (bindingResult.hasErrors()) {
            setListModel(model, admin);
            model.addAttribute("openScholarshipModal", true);
            model.addAttribute("formError", ValidationMessages.firstError(bindingResult));
            return "scholarship/scholarship";
        }

        scholarshipService.createScholarship(dto, admin);
        return "redirect:/scholarships";
    }

    /**
     * 기존 장학금을 수정한다.
     *
     * @param id 장학금 PK
     * @param dto 장학금 수정 정보
     * @param bindingResult 검증 결과
     * @param model 화면에 전달할 데이터
     * @param principal 로그인 관리자 정보
     * @return 장학금 목록 화면 또는 리다이렉트
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("scholarshipDTO") ScholarshipDTO dto,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Admin admin = principal.getAdmin();

        if (bindingResult.hasErrors()) {
            setListModel(model, admin);
            model.addAttribute("openScholarshipModal", true);
            model.addAttribute("editingScholarshipId", id);
            model.addAttribute("formError", ValidationMessages.firstError(bindingResult));
            return "scholarship/scholarship";
        }

        scholarshipService.updateScholarship(id, dto, admin);
        return "redirect:/scholarships";
    }

    /**
     * 장학금을 삭제한다.
     *
     * @param id 장학금 PK
     * @param principal 로그인 관리자 정보
     * @return 장학금 목록 화면으로 리다이렉트
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        scholarshipService.deleteScholarship(id, principal.getAdmin());
        return "redirect:/scholarships";
    }

    /**
     * 장학금 목록 화면에 필요한 공통 데이터를 구성한다.
     *
     * @param model 화면에 전달할 모델
     * @param admin 로그인 관리자
     */
    private void setListModel(Model model, Admin admin) {
        model.addAttribute("scholarships", toListItems(scholarshipService.findScholarshipsFor(admin)));
        model.addAttribute("totalCount", scholarshipService.countAll(admin));
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("linkableNotices", noticeService.findAllForLink(admin));
    }

    /**
     * Scholarship 엔티티 목록을 화면 표시용 DTO 목록으로 변환한다.
     *
     * @param scholarships 변환할 장학금 엔티티 목록
     * @return 화면 표시용 장학금 DTO 목록
     */
    private List<ScholarshipDTO> toListItems(List<Scholarship> scholarships) {
        return scholarships.stream()
                .map(this::toListItem)
                .toList();
    }

    /**
     * Scholarship 엔티티를 화면 표시용 DTO로 변환한다.
     *
     * @param scholarship 변환할 장학금 엔티티
     * @return 화면 표시용 장학금 DTO
     */
    private ScholarshipDTO toListItem(Scholarship scholarship) {
        ScholarshipDTO dto = new ScholarshipDTO();

        dto.setScholarshipId(scholarship.getScholarshipId());
        dto.setName(scholarship.getName());
        dto.setType(scholarship.getType());
        dto.setDeptId(
                scholarship.getDepartment() != null
                        ? scholarship.getDepartment().getDeptId()
                        : null
        );
        dto.setTargetGrade(scholarship.getTargetGrade());
        dto.setResidenceCondition(scholarship.getResidenceCondition());
        dto.setDeadline(scholarship.getDeadline());
        dto.setVisible(scholarship.getVisible());
        dto.setNoticeId(
                scholarship.getNotice() != null
                        ? scholarship.getNotice().getNoticeId()
                        : null
        );

        return dto;
    }
}