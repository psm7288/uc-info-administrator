package uc.dev.uc_info.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 학사 일정 등록/수정 폼 요청 + 목록 화면 표시 겸용 DTO.
 *
 * <p>schedule.html 의 일정 등록/수정 모달(scheduleModal) 입력값을 받는다.
 * 컨트롤러에서 {@code @Valid @ModelAttribute("scheduleDTO") ScheduleDTO dto}
 * 로 바인딩 + 검증한다.</p>
 *
 * <p>공지(Notice)와 마찬가지로 대상 학과(deptId)가 비면 전체 대상, targetGrade 가 비면
 * 전체 학년이다. 권한 판단(DEPT_ADMIN 은 본인 학과/전체만 지정 가능)은
 * {@link uc.dev.uc_info.service.ScheduleService} 에서 한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ScheduleDTO {

    /** 일정 PK. 목록 표시/수정 모달 프리필 전용(등록 폼에는 없음). */
    private Long scheduleId;

    /** 일정 제목. 필수. (모달 input name="title") */
    @NotBlank(message = "일정 제목은 필수입니다.")
    @Size(max = 255, message = "제목이 너무 깁니다.")
    private String title;

    /** 카테고리. ACADEMIC/EXAM/REGISTRATION/VACATION/EVENT/ETC. (name="category") */
    @NotBlank(message = "카테고리를 선택해주세요.")
    @Size(max = 20)
    private String category;

    /**
     * 대상 학과 PK(deptId). 전체 대상이면 빈 값(null). (name="deptId")
     * 화면 select option value 가 dept.deptId. 전체 허용이라 검증 없음.
     */
    private Long deptId;

    /** 대상 학년. 전체면 빈 값(null). "1"/"2"/"3" (name="targetGrade") */
    @Size(max = 10)
    private String targetGrade;

    /** 일정 시작일. 필수(엔티티 start_date NOT NULL). (name="startDate", type=date) */
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    /** 일정 종료일. 단일 일정이면 빈 값(null). (name="endDate") */
    private LocalDate endDate;

    /**
     * 노출 여부. 체크박스 등으로 받는다. (name="visible")
     * 미전송 시 기본 노출(true)로 처리한다(ScheduleService 참고).
     */
    private Boolean visible;
}