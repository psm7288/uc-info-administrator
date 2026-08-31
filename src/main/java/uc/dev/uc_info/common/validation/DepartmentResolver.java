package uc.dev.uc_info.common.validation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.repository.DepartmentRepository;

/**
 * 폼에서 넘어온 deptId(Long)를 Department 엔티티로 변환하는 공용 컴포넌트.
 * 대상 학과를 select로 지정하는 모든 등록/수정 화면(Notice/Schedule 등)에서
 * 동일하게 쓰인다.
 */
@Component
@RequiredArgsConstructor
public class DepartmentResolver {

    private final DepartmentRepository departmentRepository;

    /**
     * deptId → Department로 변환한다. deptId가 null이면 전체 학과 대상이라는
     * 의미이므로 조회하지 않고 null을 그대로 반환한다.
     *
     * @param deptId 변환할 학과 PK(전체 대상이면 null)
     * @return 조회된 Department, 또는 deptId가 null이면 null
     * @throws EntityNotFoundException deptId에 해당하는 학과가 없는 경우
     */
    public Department resolve(Long deptId) {

        if (deptId == null) {
            return null;
        }

        return departmentRepository
                .findById(deptId)
                .orElseThrow(() ->
                        new EntityNotFoundException("존재하지 않는 학과입니다.")
                );
    }
}