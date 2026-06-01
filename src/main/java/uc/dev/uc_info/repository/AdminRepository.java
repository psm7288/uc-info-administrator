package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Admin;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);
    // TODO: 인증 연동(username 등 조회), 조회용 쿼리 메서드는 핵심 로직 작업에서 추가
}