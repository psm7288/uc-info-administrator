package uc.dev.uc_info.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 계정 정보 수정 요청 DTO.
 *
 * <p>관리자명 변경과 현재 비밀번호 확인,
 * 선택적인 새 비밀번호 변경 값을 전달한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class SettingAccountDTO {

    /**
     * 변경할 관리자 표시 이름.
     */
    @NotBlank
    private String adminName;

    /**
     * 계정 정보 변경 전 본인 확인을 위한 현재 비밀번호.
     */
    @NotBlank
    private String currentPassword;

    /**
     * 변경할 새 비밀번호.
     * 비어 있으면 기존 비밀번호를 유지한다.
     */
    private String newPassword;
}