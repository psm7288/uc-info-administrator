package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학생 정보 조회 응답(GET /api/users/me).
 *
 * <p>User 엔티티를 직접 반환하지 않고 이 DTO로 변환해서 내보낸다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    /** 학생 이름 */
    private String name;

    /** 학과명 */
    private String department;

    /** 학년 */
    private Integer year;

    /** 학번 */
    private String studentId;
}