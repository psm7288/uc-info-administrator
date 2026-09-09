package uc.dev.uc_info.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.dev.uc_info.model.Schedule;
import uc.dev.uc_info.rest.dto.AcademicCalendarResponse;
import uc.dev.uc_info.service.ScheduleService;

import java.util.List;

/**
 * 학사일정 학생 앱 REST 컨트롤러. 관리자 웹의 {@link ScheduleService}를
 * 그대로 재사용하고, 응답만 {@link AcademicCalendarResponse}로 변환해서
 * 내보낸다.
 */
@RestController
@RequestMapping("/api/academic-calendar")
@RequiredArgsConstructor
public class AcademicCalendarRestController {

    private final ScheduleService scheduleService;

    /**
     * 학사일정 전체 목록을 조회한다. 인증된 학생의 소속 학과 기준으로
     * 본인 학과+전체 대상, 그리고 노출 설정(visible=true)된 일정만 나간다.
     *
     * @return 200 + 학사일정 목록
     */
    @GetMapping
    public ResponseEntity<List<AcademicCalendarResponse>> list() {
        String studentId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<AcademicCalendarResponse> calendar = scheduleService.findVisibleForStudent(studentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(calendar);
    }

    /**
     * Schedule 엔티티를 응답 DTO로 변환한다.
     *
     * @param schedule 변환할 일정
     * @return 변환된 응답 DTO
     */
    private AcademicCalendarResponse toResponse(Schedule schedule) {
        AcademicCalendarResponse response = new AcademicCalendarResponse();

        response.setId(schedule.getScheduleId());
        response.setTitle(schedule.getTitle());
        response.setStartDate(schedule.getStartDate());
        response.setEndDate(schedule.getEndDate());
        response.setCategory(schedule.getCategory());

        return response;
    }
}