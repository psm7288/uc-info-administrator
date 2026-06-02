package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Shuttle;

public interface ShuttleRepository extends JpaRepository<Shuttle, Long> {

}