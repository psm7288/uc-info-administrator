package uc.dev.uc_info.common.util;

/**
 * 여러 도메인에서 반복되는 간단한 문자열 정규화 유틸리티. 상태 없는 순수
 * 함수만 모아둔다 — Spring 빈으로 등록하지 않는다.
 */
public class TextNormalizer {

    private TextNormalizer(){}

    /**
     * 공백뿐인 문자열을 null로 바꾼다. targetGrade처럼 "선택 안 함"이 빈
     * 문자열로 넘어오는 필드를 "전체 대상(null)"으로 정규화할 때 쓴다.
     *
     * @param value 원본 문자열
     * @return value가 null이거나 공백뿐이면 null, 아니면 value 그대로
     */
    public static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}