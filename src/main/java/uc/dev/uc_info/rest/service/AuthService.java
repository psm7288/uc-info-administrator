package uc.dev.uc_info.rest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.model.User;
import uc.dev.uc_info.repository.UserRepository;
import uc.dev.uc_info.rest.dto.AuthResponse;
import uc.dev.uc_info.rest.dto.VerifyRequest;
import uc.dev.uc_info.security.jwt.JwtTokenProvider;

/**
 * 학생 앱 인증 서비스. 이름+학과+학번으로 본인 확인 후 JWT를 발급한다.
 *
 * <p>REST DTO({@link VerifyRequest}/{@link AuthResponse})의 필드명은
 * {@code studentId}지만(Flutter 쪽 JSON 계약), User 엔티티의 실제 필드명은
 * {@code studentNumber}다 </p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 이름+학과+학번이 일치하는 학생을 확인하고 토큰을 발급한다. 어느
     * 값이 틀렸는지는 구분해서 알려주지 않는다
     *
     * @param request 인증 요청(이름/학과/학번)
     * @return 토큰과 학생 정보를 담은 응답
     * @throws AccessDeniedException 학번이 없거나, 이름/학과 중 하나라도 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public AuthResponse verify(VerifyRequest request) {
        User user = userRepository.findByStudentNumber(request.getStudentId())
                .orElseThrow(() -> new AccessDeniedException("인증 정보가 일치하지 않습니다."));

        boolean nameMatches = user.getUserName().equals(request.getName());
        boolean deptMatches = user.getDepartment() != null
                && user.getDepartment().getDeptName().equals(request.getDepartment());

        if (!nameMatches || !deptMatches) {
            throw new AccessDeniedException("인증 정보가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getStudentNumber());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setName(user.getUserName());
        response.setDepartment(user.getDepartment().getDeptName());
        response.setYear(user.getGrade());
        response.setStudentId(user.getStudentNumber());

        return response;
    }
}