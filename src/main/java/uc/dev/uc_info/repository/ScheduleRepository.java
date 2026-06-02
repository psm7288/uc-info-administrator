package uc.dev.uc_info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uc.dev.uc_info.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

}