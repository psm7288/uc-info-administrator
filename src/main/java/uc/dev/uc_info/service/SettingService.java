package uc.dev.uc_info.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uc.dev.uc_info.dto.SettingAccountDTO;
import uc.dev.uc_info.dto.SettingAlarmDTO;
import uc.dev.uc_info.model.Admin;
import uc.dev.uc_info.repository.AdminRepository;

/**
 * 관리자 본인 계정 설정 서비스. 다른 도메인과 달리 대상이 항상 "로그인한
 * 본인"이라 권한 검증(AdminScopeValidator)이 필요 없다 — admin 파라미터
 * 자체가 이미 본인이다.
 *
 * <p>이 클래스 안에 아래 2개의 public 메서드를 직접 작성하세요.</p>
 *
 * <h3>만들어야 할 메서드</h3>
 * <ol>
 *   <li>{@code Admin updateAccount(SettingAccountDTO dto, Admin admin)}
 *       — {@code passwordEncoder.matches(dto.getCurrentPassword(),
 *       admin.getPassword())}로 현재 비밀번호를 먼저 확인한다. 틀리면
 *       {@code IllegalArgumentException}("현재 비밀번호가 일치하지 않습니다."). 통과하면
 *       {@code admin.setAdminName(dto.getAdminName())}. {@code dto.getNewPassword()}가
 *       비어있지 않으면 {@code passwordEncoder.encode(...)}로 해싱해서
 *       {@code admin.setPassword(...)}(비어있으면 비밀번호는 그대로 둠).
 *       {@code adminRepository.save(admin)} 후 반환.</li>
 *   <li>{@code Admin updateAlarm(SettingAlarmDTO dto, Admin admin)}
 *       — {@code admin.setAlarmTracking(dto.getAlarmTracking() != null &&
 *       dto.getAlarmTracking())}, {@code alarmSend}도 동일하게 null-safe 처리
 *       (체크 해제 시 null로 들어오므로 false로 취급). save 후 반환.</li>
 * </ol>
 *
 * <h3>⚠️ 반드시 알아야 할 것 — SecurityContext 갱신 문제</h3>
 * <p>로그인 시 {@code Authentication}에 담긴 {@code Admin} 객체는 DB를 수정해도
 * 자동으로 안 바뀐다. 즉 이름을 바꿔도 재로그인 전까지 사이드바/헤더에는
 * 옛날 이름이 계속 보인다. 이번 라운드에서 이 문제까지 고칠지는 별도
 * 논의 후 결정 — 일단 DB 저장까지만 구현하고, 화면에 바로 반영 안 되는
 * 현상이 보이면 그건 버그가 아니라 이 문제이니 별도로 알릴 것.</p>
 *
 * <h3>이 서비스가 던지는 예외</h3>
 * <ul>
 *   <li>{@code IllegalArgumentException} — 현재 비밀번호가 틀린 경우.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SettingService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
}