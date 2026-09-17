package uc.dev.uc_info.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 공지 작성/수정 폼 요청 + 목록 화면 표시 겸용 DTO.
 *
 * <p>notice-write.html / notice-edit.html 의 입력값을 받는다. 컨트롤러에서
 * {@code @Valid @ModelAttribute NoticeDTO dto} 로 바인딩 + 검증하며, 폼의 name
 * 속성과 아래 필드명이 같아야 값이 자동으로 들어온다.</p>
 *
 * <p>작성/수정 화면 모두 저장 버튼의 name="status" 값 하나로 통일한다
 * ("DRAFT"=임시저장 / "PUBLISHED"=게시).</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class NoticeDTO {

    /** 공지 PK. 목록 표시 전용(폼에는 없음, 수정 링크/삭제 폼에 사용). */
    private Long noticeId;

    /** 작성자 이름. 목록 표시 전용(Notice.admin.adminName 을 미리 평탄화한 값). */
    private String adminName;

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

    /** 대상 학과 PK(deptId). 전체 대상이면 빈 값(null)으로 들어온다. (name="deptId") */
    private Long deptId;

    /** 대상 학년. 전체면 빈 값(null). "1"/"2"/"3" (name="targetGrade") */
    @Size(max = 10)
    private String targetGrade;

    /** 게시 시작일. (name="startDate", type=date → yyyy-MM-dd) */
    private LocalDate startDate;

    /** 게시 종료일. (name="endDate") */
    private LocalDate endDate;

    /**
     * 저장 버튼 값. "DRAFT"(임시저장) / "PUBLISHED"(게시). 작성/수정 화면
     * 공통으로 이 필드 하나만 쓴다. (name="status")
     */
    private String status;

    /** 상단 고정 여부. 목록 표시 전용. */
    private boolean pinned;

    /** 푸시 발송 여부. 목록 표시 전용. */
    private boolean pushSent;
}