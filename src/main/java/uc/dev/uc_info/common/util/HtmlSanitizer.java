package uc.dev.uc_info.common.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 리치 에디터에서 온 HTML을 저장 전에 정제(sanitize)한다. {@code <script>}
 * 등 위험한 태그/속성을 제거해 저장형 XSS를 막는다. 상태 없는 순수 함수만
 * 모아둔다 — Spring 빈으로 등록하지 않는다.
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {
    }

    /**
     * HTML을 허용된 태그(b/i/u/ul/ol/li/a 등)만 남기고 정제한다. 에디터
     * 툴바가 지원하는 서식(굵게/기울임/밑줄/목록/링크)과 일치하는 jsoup의
     * 기본 허용목록({@code Safelist.basic()})을 그대로 쓴다.
     *
     * @param html 원본 HTML(null 가능)
     * @return 정제된 HTML. null이면 빈 문자열
     */
    public static String sanitize(String html) {
        if (html == null) {
            return "";
        }
        return Jsoup.clean(html, Safelist.basic());
    }
}