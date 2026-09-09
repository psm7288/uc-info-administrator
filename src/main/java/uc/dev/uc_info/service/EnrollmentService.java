package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.model.CourseOffering;
import uc.dev.uc_info.model.Enrollment;
import uc.dev.uc_info.model.User;
import uc.dev.uc_info.repository.CourseOfferingRepository;
import uc.dev.uc_info.repository.EnrollmentRepository;
import uc.dev.uc_info.repository.UserRepository;

import java.util.List;

/**
 * 수강선택(Enrollment) 비즈니스 로직 서비스. 학생이 개설과목을 담고 빼는
 * 것, 학생 본인 시간표 조회, 관리자의 학생별 시간표 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final int COLOR_COUNT = 6;

    private final EnrollmentRepository enrollmentRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final UserRepository userRepository;

    /**
     * 학생이 개설과목을 담는다(수강선택 생성). 색상은 현재까지 담은 과목
     * 수를 6으로 나눈 나머지로 자동 배정한다(학생이 직접 안 고름).
     *
     * @param studentId        담으려는 학생의 학번(토큰에서 추출)
     * @param courseOfferingId 담으려는 개설과목 PK
     * @return 생성된 수강선택
     * @throws EntityNotFoundException studentId 또는 courseOfferingId가 존재하지 않는 경우
     * @throws IllegalStateException   이미 담은 과목을 다시 담으려는 경우
     */
    @Transactional
    public Enrollment enroll(String studentId, Long courseOfferingId) {
        User user = getStudentOrThrow(studentId);

        CourseOffering offering = courseOfferingRepository.findById(courseOfferingId)
                .orElseThrow(() -> new EntityNotFoundException("개설과목을 찾을 수 없습니다."));

        boolean alreadyEnrolled = enrollmentRepository
                .existsByUser_UserIdAndCourseOffering_CourseOfferingId(user.getUserId(), courseOfferingId);

        if (alreadyEnrolled) {
            throw new IllegalStateException("이미 담은 과목입니다.");
        }

        long currentCount = enrollmentRepository.countByUser_UserId(user.getUserId());

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourseOffering(offering);
        enrollment.setColor((int) (currentCount % COLOR_COUNT));

        return enrollmentRepository.save(enrollment);
    }

    /**
     * 학생이 담았던 과목을 뺀다(수강선택 삭제).
     * 본인이 담은 것만 뺄 수 있다
     *
     * @param studentId    요청한 학생의 학번(토큰에서 추출)
     * @param enrollmentId 뺄 수강선택 PK
     * @throws EntityNotFoundException studentId에 해당하는 학생이 없거나,
     *                                 본인이 담은 게 아니거나 존재하지 않는 enrollmentId인 경우
     */
    @Transactional
    public void unenroll(String studentId, Long enrollmentId) {
        User user = getStudentOrThrow(studentId);

        Enrollment enrollment = enrollmentRepository
                .findByEnrollmentIdAndUser_UserId(enrollmentId, user.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("수강선택 정보를 찾을 수 없습니다."));

        enrollmentRepository.delete(enrollment);
    }

    /**
     * 학생 본인의 시간표(담은 과목 전체)를 조회한다.
     * GET /api/schedule/me에서 사용한다.
     *
     * @param studentId 조회 요청 학생의 학번(토큰에서 추출)
     * @return 담은 과목 목록
     * @throws EntityNotFoundException studentId에 해당하는 학생이 없는 경우
     */
    @Transactional(readOnly = true)
    public List<Enrollment> getScheduleForStudent(String studentId) {
        User user = getStudentOrThrow(studentId);
        return enrollmentRepository.findByUser_UserId(user.getUserId());
    }

    /**
     * 관리자가 학번으로 특정 학생의 시간표(담은 과목 전체)를 조회한다.
     * 관리자 화면의 "학번 검색" 기능에서 쓰인다.
     *
     * @param studentNumber 조회할 학생의 학번
     * @return 그 학생이 담은 과목 목록. 학번이 없거나 담은 과목이 없으면 빈 목록
     */
    @Transactional(readOnly = true)
    public List<Enrollment> getScheduleForAdmin(String studentNumber) {
        return userRepository.findByStudentNumber(studentNumber)
                .map(user -> enrollmentRepository.findByUser_UserId(user.getUserId()))
                .orElse(List.of());
    }

    /**
     * studentId로 학생을 조회한다. 학생 앱 REST 메서드들이 공통으로 쓰는
     * 조회라 중복을 없애기 위해 뽑아냈다.
     *
     * @param studentId 조회할 학번
     * @return 조회된 학생
     * @throws EntityNotFoundException 해당 학번의 학생이 없는 경우
     */
    private User getStudentOrThrow(String studentId) {
        return userRepository.findByStudentNumber(studentId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }
}