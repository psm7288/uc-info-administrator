package uc.dev.uc_info.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.dev.uc_info.model.CourseOffering;
import uc.dev.uc_info.rest.dto.CourseOfferingResponse;
import uc.dev.uc_info.service.CourseOfferingService;

import java.util.List;

/**
 * 개설과목 학생 앱 REST 컨트롤러. 학생이 담을 수 있는(본인 학과)
 * 개설과목 목록을 제공한다.
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseOfferingRestController {

    private final CourseOfferingService courseOfferingService;

    /**
     * 본인 학과 개설과목 목록을 조회한다.
     *
     * @return 200 + 개설과목 목록
     */
    @GetMapping
    public ResponseEntity<List<CourseOfferingResponse>> list() {
        String studentId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<CourseOfferingResponse> offerings = courseOfferingService.findAvailableForStudent(studentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(offerings);
    }

    /**
     * CourseOffering 엔티티를 응답 DTO로 변환한다.
     *
     * @param offering 변환할 개설과목
     * @return 변환된 응답 DTO
     */
    private CourseOfferingResponse toResponse(CourseOffering offering) {
        CourseOfferingResponse response = new CourseOfferingResponse();

        response.setId(offering.getCourseOfferingId());
        response.setSubject(offering.getSubject());
        response.setDay(offering.getDay());
        response.setStartHour(offering.getStartHour());
        response.setEndHour(offering.getEndHour());
        response.setRoom(offering.getRoom());
        response.setProfessor(offering.getProfessor());

        return response;
    }
}