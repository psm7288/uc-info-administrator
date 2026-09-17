package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.model.Department;
import uc.dev.uc_info.repository.DepartmentRepository;

import java.util.List;

/**
 * 학과(Department) 조회를 담당하는 서비스.
 *
 * <p>컨트롤러가 DepartmentRepository를 직접 호출하지 않고
 * 서비스 계층을 통해 학과 데이터를 조회하도록 하기 위해 사용한다.</p>
 *
 * <p>현재는 학사 일정 등록/수정 모달에서 대상 학과 선택 목록을
 * 출력하기 위한 전체 학과 조회 기능을 제공한다.</p>
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    /**
     * 학과 데이터에 접근하기 위한 Repository.
     *
     * <p>Department 테이블의 조회 기능을 담당하며,
     * 생성자 주입은 Lombok의 @RequiredArgsConstructor가 자동으로 생성한다.</p>
     */
    private final DepartmentRepository departmentRepository;

    /**
     * 등록되어 있는 전체 학과 목록을 조회한다.
     *
     * <p>ScheduleController에서 일정 등록/수정 모달의
     * 대상 학과 select 박스를 구성할 때 사용한다.</p>
     *
     * <p>조회 전용 메서드이므로 readOnly=true 트랜잭션을 사용한다.</p>
     *
     * @return 전체 학과 목록
     */
    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }
}