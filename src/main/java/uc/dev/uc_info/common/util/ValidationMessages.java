package uc.dev.uc_info.common.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * {@code @Valid} 검증 실패 시 화면에 보여줄 메시지를 뽑는 유틸리티. 폼이
 * 여러 필드를 갖고 있어도 사용자에게는 "가장 먼저 걸린 것 하나"만 간단히
 * 보여주는 걸로 통일한다. 상태 없는 순수 함수만 모아둔다 — Spring 빈으로
 * 등록하지 않는다.
 */
public final class ValidationMessages {

    private ValidationMessages(){
    }

    /**
     * BindingResult의 필드 오류 중 첫 번째 메시지를 반환한다. DTO의
     * {@code @NotBlank(message = "...")} 등에 지정된 문구가 그대로 나온다.
     *
     * @param bindingResult 검증 결과
     * @return 첫 번째 필드 오류 메시지, 없으면 일반 안내 문구
     */
    public static String firstError(BindingResult bindingResult){
        return bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값을 확인해주세요.");
    }
}