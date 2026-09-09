package uc.dev.uc_info.rest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.model.User;
import uc.dev.uc_info.repository.UserRepository;
import uc.dev.uc_info.rest.dto.UserResponse;

/**
 * 학생 본인 정보 조회 서비스. Flutter 학생 앱의 GET /api/users/me에서만
 * 쓰인다. 인증 자체(토큰 발급)는 {@link AuthService}가 담당하고, 이
 * 클래스는 "이미 인증된 학생의 정보를 조회"하는 것만 책임진다
 *
 * <p>REST DTO({@link UserResponse})의 필드명은 {@code studentId}지만
 * (Flutter 쪽 JSON 계약), User 엔티티의 실제 필드명은 {@code studentNumber}다</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 토큰에서 꺼낸 studentId(=User.studentNumber)로 본인 정보를 조회한다.
     *
     * @param studentId 토큰에서 추출한 학번(JwtAuthenticationFilter가 SecurityContext에 세팅)
     * @return 학생 정보 응답 DTO(민감정보 제외)
     * @throws AccessDeniedException 토큰은 유효한데 해당 학번 학생이 DB에 없는 경우(탈퇴 등)
     */
    @Transactional(readOnly = true)
    public UserResponse getMe(String studentId) {
        User user = userRepository.findByStudentNumber(studentId)
                .orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다."));

        UserResponse response = new UserResponse();
        response.setName(user.getUserName());
        response.setDepartment(user.getDepartment() != null ? user.getDepartment().getDeptName() : null);
        response.setYear(user.getGrade());
        response.setStudentId(user.getStudentNumber());

        return response;
    }
}