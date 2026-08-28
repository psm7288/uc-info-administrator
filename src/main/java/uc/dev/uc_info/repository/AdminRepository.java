package uc.dev.uc_info.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uc.dev.uc_info.model.Admin;

/**
 * 관리자(Admin) 영속성 접근 인터페이스. 로그인 인증용 조회 메서드 포함.
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * username으로 Admin 조회 + department 즉시 로딩(fetch join). department가
     * LAZY라 세션 종료 후(로그인 이후 요청) 접근 시 LazyInitializationException이
     * 나서 미리 당겨온다. SUPER_ADMIN은 department가 null이라 INNER 대신
     * LEFT JOIN 사용(안 그러면 그 계정이 조회 결과에서 통째로 빠짐).
     *
     * @param username 로그인 아이디
     * @return 조회된 Admin(department 포함), 없으면 empty
     */
    @Query("""
        SELECT a FROM Admin a
        LEFT JOIN FETCH a.department
        WHERE a.username = :username
        """)
    Optional<Admin> findByUsernameWithDepartment(@Param("username") String username);
}