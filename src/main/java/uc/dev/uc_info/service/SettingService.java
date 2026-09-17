package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uc.dev.uc_info.dto.SettingAccountDTO;
import uc.dev.uc_info.dto.SettingAlarmDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.repository.AdminRepository;

/**
 * 관리자 본인의 계정 정보와 알림 설정을 관리하는 서비스.
 *
 * <p>현재 로그인한 관리자 객체를 기준으로 계정 정보와 알림 설정을 변경한다.</p>
 */
@Service
@RequiredArgsConstructor
public class SettingService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인한 관리자의 계정 정보를 수정한다.
     *
     * <p>현재 비밀번호가 일치하는지 확인한 뒤 관리자명을 수정하며,
     * 새 비밀번호가 입력된 경우 BCrypt로 암호화하여 저장한다.</p>
     *
     * @param dto 변경할 계정 정보
     * @param admin 로그인한 관리자(department까지 이미 로드된 상태)
     * @return 수정 반영된, 세션에 안전하게 재사용 가능한 관리자(파라미터와 동일 참조)
     * @throws IllegalArgumentException 현재 비밀번호가 일치하지 않거나, 새 비밀번호가 8자 미만인 경우
     */
    @Transactional
    public Admin updateAccount(SettingAccountDTO dto, Admin admin) {

        if (!passwordEncoder.matches(dto.getCurrentPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        admin.setAdminName(dto.getAdminName());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getNewPassword().length() < 8) {
                    throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다."
                );
            }
            admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }
        adminRepository.save(admin);

        return admin;
    }

    /**
     * 로그인한 관리자의 알림 설정을 수정한다.
     *
     * <p>체크되지 않은 항목은 {@code null}로 전달될 수 있으므로
     * {@link Boolean#TRUE} 비교를 통해 안전하게 false로 처리한다.</p>
     *
     * @param dto 변경할 알림 설정
     * @param admin 로그인한 관리자(department까지 이미 로드된 상태)
     * @return 수정 반영된, 세션에 안전하게 재사용 가능한 관리자(파라미터와 동일 참조)
     */
    @Transactional
    public Admin updateAlarm(SettingAlarmDTO dto, Admin admin) {
        admin.setAlarmTracking(Boolean.TRUE.equals(dto.getAlarmTracking()));
        admin.setAlarmSend(Boolean.TRUE.equals(dto.getAlarmSend()));
        adminRepository.save(admin);

        return admin;
    }
}