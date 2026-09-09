package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공지 목록/상세 응답(GET /api/notices, GET /api/notices/{id}) 공용 DTO.
 * API 명세의 목록/상세 응답 형태가 동일해서 하나로 겸용한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class NoticeResponse {

    /** 공지 PK */
    private Long id;

    /** 공지 제목 */
    private String title;

    /** 표시용 날짜. "MM.dd" 형식(예: "04.28") — Notice.createdAt을 포맷한 값 */
    private String date;

    /** 카테고리: DEPARTMENT/ACADEMIC/SCHOLARSHIP/EVENT/EMPLOYMENT */
    private String category;

    /** 공지 본문(HTML) */
    private String content;
}