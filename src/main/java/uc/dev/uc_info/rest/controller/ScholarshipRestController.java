package uc.dev.uc_info.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.model.Scholarship;
import uc.dev.uc_info.rest.dto.ScholarshipResponse;
import uc.dev.uc_info.service.ScholarshipService;

import java.util.List;

/**
 * 장학금 학생 앱 REST 컨트롤러. 관리자 웹의 {@link ScholarshipService}를
 * 그대로 재사용하고, 응답만 {@link ScholarshipResponse}(실제 엔티티에 있는
 * 필드만)로 변환해서 내보낸다.
 */
@RestController
@RequestMapping("/api/scholarships")
@RequiredArgsConstructor
public class ScholarshipRestController {

    private final ScholarshipService scholarshipService;

    /**
     * 유형별 장학금 목록을 조회한다. type이 없으면 전체를 반환한다.
     * 인증된 학생의 소속 학과 기준으로 본인 학과+전체 대상 장학금만 나간다.
     *
     * @param type 좁힐 유형(선택, REGIONAL/GRADE/INTERNAL/EXTERNAL)
     * @return 200 + 장학금 목록
     */
    @GetMapping
    public ResponseEntity<List<ScholarshipResponse>> list(
            @RequestParam(required = false) String type
    ) {
        String studentId = currentStudentId();

        List<ScholarshipResponse> scholarships = scholarshipService.findVisibleForStudent(studentId, type)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(scholarships);
    }

    /**
     * 장학금 상세를 조회한다. 비노출이거나 다른 학과 전용이면 404로
     * 응답한다.
     *
     * @param id 조회할 장학금 PK
     * @return 200 + 장학금 상세
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScholarshipResponse> detail(@PathVariable Long id) {
        String studentId = currentStudentId();
        Scholarship scholarship = scholarshipService.getVisibleScholarshipForStudent(id, studentId);

        return ResponseEntity.ok(toResponse(scholarship));
    }

    /**
     * SecurityContext에서 현재 인증된 학생의 studentId를 꺼낸다.
     *
     * @return 토큰에서 추출한 studentId
     */
    private String currentStudentId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Scholarship 엔티티를 응답 DTO로 변환한다.
     *
     * @param scholarship 변환할 장학금
     * @return 변환된 응답 DTO
     */
    private ScholarshipResponse toResponse(Scholarship scholarship) {
        ScholarshipResponse response = new ScholarshipResponse();

        response.setId(scholarship.getScholarshipId());
        response.setTitle(scholarship.getName());
        response.setType(scholarship.getType());
        response.setDeadline(scholarship.getDeadline());

        return response;
    }
}