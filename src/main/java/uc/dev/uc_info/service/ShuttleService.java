package uc.dev.uc_info.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.dto.ShuttleDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.model.Shuttle;
import uc.dev.uc_info.repository.ShuttleRepository;

import java.util.List;

/**
 * 셔틀버스 노선(Shuttle) 비즈니스 로직 서비스. {@code /shuttles/**}가
 * SecurityConfig에서 이미 {@code hasRole("SUPER_ADMIN")}으로 막혀있어, 이
 * 서비스에 도달한 시점엔 SUPER_ADMIN이 보장된다
 */
@Service
@RequiredArgsConstructor
public class ShuttleService {

    private final ShuttleRepository shuttleRepository;

    /**
     * 전체 노선 목록을 조회한다. 권한 분기 없이 전체 하나만 있으면 된다
     * (SUPER_ADMIN만 도달 가능).
     *
     * @return 전체 노선 목록(최신순)
     */
    @Transactional(readOnly = true)
    public List<Shuttle> findAll() {
        return shuttleRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 노선 단건을 PK로 조회한다.
     *
     * @param id 노선 PK
     * @return 조회된 노선 엔티티
     * @throws IllegalArgumentException id가 null인 경우
     * @throws EntityNotFoundException  해당 id의 노선이 없는 경우
     */
    @Transactional(readOnly = true)
    public Shuttle getShuttle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("셔틀 노선 ID는 null일 수 없습니다.");
        }
        return shuttleRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("셔틀 노선을 찾을 수 없습니다."));
    }

    /**
     * 전체 노선 개수. 화면 상단 통계용.
     *
     * @return 전체 노선 건수
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return shuttleRepository.count();
    }

    /**
     * 운영 중(status=ACTIVE) 노선 개수. 화면 상단 "운영 노선 수" 통계용.
     *
     * @return ACTIVE 상태 노선 건수
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return shuttleRepository.countByStatus("ACTIVE");
    }

    /**
     * 새 셔틀 노선을 등록한다. routeName/departure/destination/status
     * 공백 검증은 DTO의 {@code @NotBlank}와 컨트롤러의 BindingResult가
     * 이미 처리했다고 보고 여기서 재검사하지 않는다.
     *
     * @param dto   등록 폼 DTO
     * @param admin 등록자(로그인 관리자, 항상 SUPER_ADMIN)
     * @return 저장된 노선
     */
    @Transactional
    public Shuttle createShuttle(ShuttleDTO dto, Admin admin) {
        Shuttle shuttle = new Shuttle();

        shuttle.setAdmin(admin);
        shuttle.setRouteName(dto.getRouteName());
        shuttle.setDeparture(dto.getDeparture());
        shuttle.setDestination(dto.getDestination());
        shuttle.setWaypoints(dto.getWaypoints());
        shuttle.setFirstDeparture(dto.getFirstDeparture());
        shuttle.setLastDeparture(dto.getLastDeparture());
        shuttle.setStatus(dto.getStatus());

        return shuttleRepository.save(shuttle);
    }

    /**
     * 기존 셔틀 노선을 수정한다. 등록자(admin) 필드는 변경하지 않는다.
     * "운행중단"도 삭제가 아니라 이 메서드로 status를 SUSPENDED로 바꿔서
     * 처리한다(별도 메서드 불필요).
     *
     * @param id    수정할 노선 PK
     * @param dto   수정 폼 DTO
     * @param admin 수정 요청 관리자(항상 SUPER_ADMIN, 권한 검증 불필요)
     * @return 수정된 노선
     * @throws EntityNotFoundException 없는 id인 경우
     */
    @Transactional
    public Shuttle updateShuttle(Long id, ShuttleDTO dto, Admin admin) {
        Shuttle shuttle = getShuttle(id);

        shuttle.setRouteName(dto.getRouteName());
        shuttle.setDeparture(dto.getDeparture());
        shuttle.setDestination(dto.getDestination());
        shuttle.setWaypoints(dto.getWaypoints());
        shuttle.setFirstDeparture(dto.getFirstDeparture());
        shuttle.setLastDeparture(dto.getLastDeparture());
        shuttle.setStatus(dto.getStatus());

        return shuttleRepository.save(shuttle);
    }

    /**
     * 셔틀 노선을 삭제한다(물리 삭제). 다른 엔티티가 FK로 참조하지 않는
     * 구조라 소프트 삭제 없이 바로 삭제해도 무방하다.
     *
     * @param id    삭제할 노선 PK
     * @param admin 삭제 요청 관리자(항상 SUPER_ADMIN, 권한 검증 불필요)
     * @throws EntityNotFoundException 없는 id인 경우
     */
    @Transactional
    public void deleteShuttle(Long id, Admin admin) {
        Shuttle shuttle = getShuttle(id);
        shuttleRepository.delete(shuttle);
    }
}