package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Shuttle;

import java.util.List;

public interface ShuttleRepository extends JpaRepository<Shuttle, Long> {

    List<Shuttle> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}