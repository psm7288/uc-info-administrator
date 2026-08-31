package uc.dev.uc_info.common.validation;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Department;

/**
 * "SUPER_ADMIN은 전체, DEPT_ADMIN은 본인 학과+전체 대상만"이라는 공통 권한
 * 규칙을 한 곳에서 처리한다.
 */
@Component
public class AdminScopeValidator {

    /**
     * admin.role이 "SUPER_ADMIN"인지 확인한다.
     *
     * @param admin 확인할 관리자
     * @return SUPER_ADMIN이면 true
     */
    public boolean isSuperAdmin(Admin admin) {
        return "SUPER_ADMIN".equals(String.valueOf(admin.getRole()));
    }

    /**
     * admin.role이 "DEPT_ADMIN"인지 확인한다.
     *
     * @param admin 확인할 관리자
     * @return DEPT_ADMIN이면 true
     */
    public boolean isDeptAdmin(Admin admin) {
        return "DEPT_ADMIN".equals(String.valueOf(admin.getRole()));
    }

    /**
     * 관리자가 이 대상 학과를 지정(등록/변경)할 권한이 있는지 검사한다.
     * SUPER_ADMIN은 항상 통과, DEPT_ADMIN은 본인 학과 또는 전체(null)만
     * 지정 가능하다.
     *
     * @param admin      권한을 확인할 관리자
     * @param department 지정하려는 대상 학과(전체면 null)
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우(데이터 이상)
     * @throws AccessDeniedException DEPT_ADMIN이 다른 학과를 지정하거나, 권한 자체가 없는 경우
     */
    public void validateAssignable(Admin admin, Department department) {
        if (isSuperAdmin(admin)) {
            return;
        }

        if (isDeptAdmin(admin)) {

            if (admin.getDepartment() == null) {
                throw new IllegalStateException("학과 관리자의 소속 학과가 없습니다.");
            }

            if (department == null) {
                return;
            }

            Long adminDeptId = admin.getDepartment().getDeptId();
            Long targetDeptId = department.getDeptId();
            if (!adminDeptId.equals(targetDeptId)) {
                throw new AccessDeniedException("다른 학과의 항목을 등록/변경할 수 없습니다.");
            }
            return;
        }
        throw new AccessDeniedException("이 기능을 사용할 권한이 없습니다.");
    }

    /**
     * 이미 존재하는 항목(수정/삭제 대상)에 대한 접근 권한을 검사한다.
     * SUPER_ADMIN은 항상 통과, DEPT_ADMIN은 항목의 대상 학과가 null(전체
     * 대상)이거나 본인 소속 학과와 같아야 통과한다.
     *
     * @param admin  권한을 확인할 관리자
     * @param target 접근하려는 기존 항목({@link DepartmentScoped} 구현체)
     * @throws AccessDeniedException 권한이 없거나 다른 학과 소속 항목에 접근하는 경우
     * @throws IllegalStateException DEPT_ADMIN인데 소속 학과가 없는 경우(데이터 이상)
     */
    public void validateAccess(Admin admin, DepartmentScoped target) {

        if (isSuperAdmin(admin)) {
            return;
        }

        if (!isDeptAdmin(admin)) {
            throw new AccessDeniedException("이 항목에 접근할 권한이 없습니다.");
        }

        if (admin.getDepartment() == null) {
            throw new IllegalStateException("관리자의 학과 정보가 없습니다.");
        }

        if (target.getDepartment() == null) {
            return;
        }

        Long adminDeptId = admin.getDepartment().getDeptId();
        Long targetDeptId = target.getDepartment().getDeptId();

        if (!adminDeptId.equals(targetDeptId)) {
            throw new AccessDeniedException("다른 학과의 항목에 접근할 수 없습니다.");
        }
    }
}