package uc.dev.uc_info.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 셔틀버스 노선 등록/수정 폼 요청 + 목록 화면 표시 겸용 DTO. 학과 개념이
 * 없는 도메인이라 deptId 같은 필드가 없다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ShuttleDTO {

    /** 노선 PK. 목록 표시 전용(등록 폼에는 없음, 수정/삭제 버튼의 data-id로 사용). */
    private Long shuttleId;

    /** 노선명. 필수. */
    @NotBlank(message = "노선명은 필수입니다.")
    private String routeName;

    /** 출발지. 필수. */
    @NotBlank(message = "출발지는 필수입니다.")
    private String departure;

    /** 도착지. 필수. */
    @NotBlank(message = "도착지는 필수입니다.")
    private String destination;

    /** 경유지 설명. 선택. */
    private String waypoints;

    /** 첫차 시간. "08:00" 같은 문자열. */
    @Size(max = 10, message = "첫차 시간이 너무 깁니다.")
    private String firstDeparture;

    /** 막차 시간. 동일 형식. */
    @Size(max = 10, message = "막차 시간이 너무 깁니다.")
    private String lastDeparture;

    /** 운행 상태: ACTIVE/SUSPENDED/DELAYED. 필수. */
    @NotBlank(message = "운행 상태를 선택해주세요.")
    private String status;
}