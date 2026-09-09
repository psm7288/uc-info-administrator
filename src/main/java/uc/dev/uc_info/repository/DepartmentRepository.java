package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Department;

import java.util.Optional;

/**
 * 학과(Department) 영속성 접근 인터페이스. 기본 CRUD는 JpaRepository가 제공한다.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 학과명으로 학과를 조회한다.
     *
     * @param deptName 조회할 학과명
     * @return 조회된 학과, 없으면 빈 Optional
     */
    Optional<Department> findByDeptName(String deptName);
}