package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 배너 목록 응답(GET /api/banners) DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class BannerResponse {

    /** 배너 PK */
    private Long id;

    /** 배너 제목 */
    private String title;

    /** 배너 부제 */
    private String subtitle;

    /** 노출 상태: ACTIVE / SCHEDULED / INACTIVE */
    private String status;
}