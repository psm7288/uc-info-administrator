//package uc.dev.uc_info.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import uc.dev.uc_info.model.Admin;
//
//public interface AdminRepository extends JpaRepository<Admin, Long> {
//
//}

// FIX: 로그인 시 username 기준 Admin 조회를 위한 findByUsername 메서드 추가
package uc.dev.uc_info.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uc.dev.uc_info.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);
}