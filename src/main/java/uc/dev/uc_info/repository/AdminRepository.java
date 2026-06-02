package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

}