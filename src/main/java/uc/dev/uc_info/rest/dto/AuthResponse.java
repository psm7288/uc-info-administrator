package uc.dev.uc_info.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학생 인증 성공 응답(POST /api/auth/verify).
 *
 * <p>Flutter 쪽은 이 토큰을 저장해뒀다가, 이후 모든 {@code /api/**} 요청의
 * {@code Authorization: Bearer {token}} 헤더에 실어 보내야 한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {

    /** 발급된 JWT. 이후 모든 /api/** 요청에 Authorization 헤더로 사용 */
    private String token;

    /** 학생 이름 */
    private String name;

    /** 학과명 */
    private String department;

    /** 학년 */
    private Integer year;

    /** 학번 */
    private String studentId;
}