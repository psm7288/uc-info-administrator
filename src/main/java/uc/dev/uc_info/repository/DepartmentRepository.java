package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}