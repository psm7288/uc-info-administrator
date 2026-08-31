package uc.dev.uc_info.common.validation;

import uc.dev.uc_info.model.Department;

/**
 * "대상 학과(department)를 가진 도메인 엔티티"임을 나타내는 마커 인터페이스.
 * Notice/Schedule 처럼 department가 null이면 전체 대상, 아니면 특정 학과
 * 전용이라는 동일한 권한 규칙을 따르는 엔티티가 구현한다.
 */
public interface DepartmentScoped {

    /**
     * 이 항목의 대상 학과.
     *
     * @return 대상 학과, 또는 전체 대상이면 null
     */
    Department getDepartment();
}