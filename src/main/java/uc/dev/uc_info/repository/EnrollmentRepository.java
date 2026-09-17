package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Enrollment;

import java.util.List;
import java.util.Optional;

/**
 * 수강선택(Enrollment) 영속성 접근 인터페이스.
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * 특정 학생이 담은 전체 과목을 조회한다. 학생 본인의 시간표
     * (GET /api/schedule/me)와 관리자의 학생별 조회 화면 양쪽에서 쓰인다.
     * 화면에 과목 정보(subject/day/...)를 같이 보여줘야 해서
     * courseOffering을 JOIN FETCH한다.
     *
     * @param userId 조회할 학생 PK
     * @return 해당 학생이 담은 수강선택 목록
     */
    @Query("""
        SELECT e
        FROM Enrollment e
        JOIN FETCH e.courseOffering
        WHERE e.user.userId = :userId
    """)
    List<Enrollment> findByUser_UserId(@Param("userId") Long userId);

    /**
     * 특정 학생이 이미 이 과목을 담았는지 확인한다. 중복 수강선택 방지용.
     *
     * @param userId           확인할 학생 PK
     * @param courseOfferingId 확인할 개설과목 PK
     * @return 이미 담았으면 true
     */
    boolean existsByUser_UserIdAndCourseOffering_CourseOfferingId(Long userId, Long courseOfferingId);

    /**
     * 특정 학생의 특정 수강선택 건을 조회한다. 삭제(빼기) 시 본인 것이
     * 맞는지 확인하는 용도(IDOR 방지)
     *
     * @param enrollmentId 조회할 수강선택 PK
     * @param userId       본인 확인용 학생 PK
     * @return 조회된 수강선택, 본인 것이 아니거나 없으면 빈 Optional
     */
    Optional<Enrollment> findByEnrollmentIdAndUser_UserId(Long enrollmentId, Long userId);

    /**
     * 특정 학생이 담은 과목 개수. 새로 담을 때 색상 인덱스(count % 6)를
     * 정하는 데 쓰인다.
     *
     * @param userId 조회할 학생 PK
     * @return 해당 학생의 수강선택 건수
     */
    long countByUser_UserId(Long userId);
}