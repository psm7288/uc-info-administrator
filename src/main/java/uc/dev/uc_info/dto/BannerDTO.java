package uc.dev.uc_info.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 배너 등록/수정 폼 요청 + 목록 화면 표시 겸용 DTO.
 *
 * <p>등록/수정 폼의 입력값을 받고, 목록 화면에서도 배너 정보를 표시할 때 사용한다.
 * Banner는 자체 학과 필드가 없으므로 연결 공지의 PK(noticeId)를 통해 권한 범위를 판단한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class BannerDTO {

    /** 배너 PK. 목록 표시 및 수정 식별용 */
    private Long bannerId;

    /** 배너 제목. 필수 */
    @NotBlank(message = "배너 제목은 필수입니다.")
    private String title;

    /** 배너 부제. 선택 */
    private String subtitle;

    /** 노출 상태: ACTIVE / SCHEDULED / INACTIVE */
    @NotBlank(message = "배너 상태를 선택해주세요.")
    private String status;

    /** 노출 시작일 */
    private LocalDate startDate;

    /** 노출 종료일 */
    private LocalDate endDate;

    /**
     * 연결 공지 PK.
     * DEPT_ADMIN은 사실상 필수이며, SUPER_ADMIN은 null 허용.
     */
    private Long noticeId;

    /**
     * 연결된 공지의 학과명(평탄화). 목록 표시 전용, 공지 미연결이면
     * "공지 미연결", 공지가 전체 대상이면 "전체 학과", 그 외엔 실제 학과명.
     */
    private String linkedDeptName;
}