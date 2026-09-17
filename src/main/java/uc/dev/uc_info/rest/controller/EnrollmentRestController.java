package uc.dev.uc_info.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uc.dev.uc_info.model.Enrollment;
import uc.dev.uc_info.rest.dto.EnrollmentRequest;
import uc.dev.uc_info.rest.dto.ScheduleResponse;
import uc.dev.uc_info.service.EnrollmentService;

import java.util.List;

/**
 * 수강선택(Enrollment) 학생 앱 REST 컨트롤러. 과목 담기/빼기와 본인
 * 시간표 조회를 제공한다.
 */
@RestController
@RequiredArgsConstructor
public class EnrollmentRestController {

    private final EnrollmentService enrollmentService;

    /**
     * 개설과목을 담는다.
     *
     * @param request 담을 과목 ID
     * @return 201(본문 없음). 이미 담은 과목이면 RestExceptionAdvice가 409로 응답
     */
    @PostMapping("/api/enrollments")
    public ResponseEntity<Void> enroll(@Valid @RequestBody EnrollmentRequest request) {
        String studentId = currentStudentId();
        enrollmentService.enroll(studentId, request.getCourseOfferingId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 담았던 과목을 뺀다. 본인이 담은 것만 뺄 수 있다(IDOR 방지는
     * Service에서 처리).
     *
     * @param id 뺄 수강선택 PK
     * @return 204 No Content
     */
    @DeleteMapping("/api/enrollments/{id}")
    public ResponseEntity<Void> unenroll(@PathVariable Long id) {
        String studentId = currentStudentId();
        enrollmentService.unenroll(studentId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 본인이 담은 과목 전체(=시간표)를 조회한다.
     *
     * @return 200 + 시간표 목록
     */
    @GetMapping("/api/schedule/me")
    public ResponseEntity<List<ScheduleResponse>> mySchedule() {
        String studentId = currentStudentId();

        List<ScheduleResponse> schedule = enrollmentService.getScheduleForStudent(studentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(schedule);
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
     * Enrollment 엔티티를 응답 DTO로 변환한다.
     *
     * @param enrollment 변환할 수강선택
     * @return 변환된 응답 DTO
     */
    private ScheduleResponse toResponse(Enrollment enrollment) {
        ScheduleResponse response = new ScheduleResponse();

        response.setEnrollmentId(enrollment.getEnrollmentId());
        response.setSubject(enrollment.getCourseOffering().getSubject());
        response.setDay(enrollment.getCourseOffering().getDay());
        response.setStartHour(enrollment.getCourseOffering().getStartHour());
        response.setEndHour(enrollment.getCourseOffering().getEndHour());
        response.setRoom(enrollment.getCourseOffering().getRoom());
        response.setProfessor(enrollment.getCourseOffering().getProfessor());
        response.setColor(enrollment.getColor());

        return response;
    }
}