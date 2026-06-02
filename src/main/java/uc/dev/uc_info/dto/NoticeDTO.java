package uc.dev.uc_info.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 공지 작성/수정 폼 요청 DTO
 *
 * <p>notice-write.html / notice-edit.html 의 입력값을 받는다. 컨트롤러에서
 * {@code @Valid @ModelAttribute NoticeDTO dto} 로 바인딩 + 검증하며, 폼의 name
 * 속성과 아래 필드명이 같아야 값이 자동으로 들어온다.</p>
 *
 * <p>작성 폼은 버튼 name="status", 수정 폼은 name="saveType" 로 다르다.
 * 두 필드를 모두 두고 작성 때는 status / 수정 때는 saveType 을 본다.</p>
 *
 * <p>검증 실패 시 컨트롤러의 BindingResult 로 잡아 폼으로 되돌린다(메시지 표시).
 * 첨부파일은 MultipartFile 로 컨트롤러에서 별도로 받는다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class NoticeDTO {

    /** 공지 제목. 필수. (폼 name="title") */
    @NotBlank(message = "공지 제목은 필수입니다.")
    @Size(max = 255, message = "제목이 너무 깁니다.")
    private String title;

    /** 공지 본문 HTML. (name="content") */
    @NotBlank(message = "공지 내용을 입력해주세요.")
    private String content;

    /** 카테고리. DEPARTMENT/ACADEMIC/SCHOLARSHIP/EVENT/EMPLOYMENT (name="category") */
    @NotBlank(message = "카테고리를 선택해주세요.")
    @Size(max = 20)
    private String category;

    /** 중요도. NORMAL/IMPORTANT/URGENT (name="priority") */
    @NotBlank(message = "중요도를 선택해주세요.")
    @Size(max = 20)
    private String priority;

    /**
     * 대상 학과 PK(deptId). 전체 대상이면 빈 값(null)으로 들어온다. (name="deptId")
     */
    private Long deptId;

    /** 대상 학년. 전체면 빈 값(null). "1"/"2"/"3" (name="targetGrade") */
    @Size(max = 10)
    private String targetGrade;

    /** 게시 시작일. (name="startDate", type=date → yyyy-MM-dd) */
    private LocalDate startDate;

    /** 게시 종료일. (name="endDate") */
    private LocalDate endDate;

    /**
     * 작성 화면 버튼 값. "DRAFT"(임시저장) / "PUBLISHED"(게시하기). (name="status")
     */
    private String status;

    /**
     * 수정 화면 버튼 값. "DRAFT" / "PUBLISHED". (name="saveType")
     */
    private String saveType;
}