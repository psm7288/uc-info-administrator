package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByDeptId(Integer deptId);

    // TODO: 조회/검색용 쿼리 메서드는 핵심 로직 작업에서 추가
}