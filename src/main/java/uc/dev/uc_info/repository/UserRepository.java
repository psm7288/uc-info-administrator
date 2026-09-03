package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.User;

/**
 * 학생(User) 영속성 접근 인터페이스. 기본 CRUD는 JpaRepository가 제공한다.
 * Tracking(열람현황)에서 "공지 대상 학생 수"를 계산하는 데 필요한 카운트
 * 메서드를 추가한다.
 */
public interface UserRepository extends JpaRepository<User, Long> {


    /**
     * 공지 대상 학생 수를 센다. 앱 접근이 허용되고(access=true) 재학 중인
     * 학생만 "대상"으로 본다(휴학/졸업생, 접근 차단 계정은 제외) —
     * 그래야 열람률이 실제 도달 가능 인원 기준으로 정확해진다.
     *
     * <p>deptId/grade가 null이면 그 조건은 무시한다(전체 대상). 공지의
     * targetGrade는 String("1"/"2"/"3")이라 호출하는 쪽(TrackingService)에서
     * Integer로 변환해서 넘겨야 한다.</p>
     *
     * @param deptId 대상 학과 PK(전체 대상이면 null)
     * @param grade  대상 학년(전체 학년이면 null)
     * @return 조건에 맞는 재학 중, 접근 허용 학생 수
     */
    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE u.access = true
              AND u.academicStatus = '재학'
              AND (:deptId IS NULL OR u.department.deptId = :deptId)
              AND (:grade IS NULL OR u.grade = :grade)
            """)
    long countTargetStudents(@Param("deptId") Long deptId, @Param("grade") Integer grade);
}