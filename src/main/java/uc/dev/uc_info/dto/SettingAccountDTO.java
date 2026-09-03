package uc.dev.uc_info.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 계정정보 수정 폼 요청 DTO(POST /settings/account).
 *
 * <p>이 클래스 안에 아래 필드들을 직접 선언하세요.</p>
 *
 * <h3>선언해야 할 필드</h3>
 * <ul>
 *   <li>{@code String adminName} — 표시 이름. 필수({@code @NotBlank}). 폼
 *       {@code name="adminName"}.</li>
 *   <li>{@code String currentPassword} — 현재 비밀번호 확인용. 필수
 *       ({@code @NotBlank}). 비밀번호를 안 바꾸더라도 본인 확인 차원에서
 *       매번 요구한다. 폼 {@code name="currentPassword"}.</li>
 *   <li>{@code String newPassword} — 새 비밀번호. 선택(검증 없음) — 비워두면
 *       "비밀번호는 안 바꾸고 이름만 수정"으로 처리한다. 폼
 *       {@code name="newPassword"}.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class SettingAccountDTO {
}