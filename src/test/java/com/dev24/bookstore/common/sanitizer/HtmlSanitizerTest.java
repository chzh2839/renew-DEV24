package com.dev24.bookstore.common.sanitizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HtmlSanitizerTest {

    private final HtmlSanitizer htmlSanitizer = new HtmlSanitizer();

    // 순수 텍스트는 그대로 통과하는지 검증
    @Test
    void sanitize_plainText_returnsUnchanged() {
        assertThat(htmlSanitizer.sanitize("정말 좋은 책이었습니다")).isEqualTo("정말 좋은 책이었습니다");
    }

    // <script> 태그와 그 안의 코드가 저장형 XSS로 이어지지 않게 제거되는지 검증
    @Test
    void sanitize_scriptTag_removesTagAndScript() {
        String result = htmlSanitizer.sanitize("좋아요<script>alert('xss')</script>");

        assertThat(result).doesNotContain("<script").doesNotContain("alert");
    }

    // <img onerror=...> 같은 이벤트 핸들러 속성 기반 XSS도 제거되는지 검증
    // (이미지는 본문 <img>가 아니라 별도 업로드로 처리하므로 IMAGES 정책을 안 넣었고, 그래서 태그째 제거된다)
    @Test
    void sanitize_imgOnErrorAttribute_removesTagAndHandler() {
        String result = htmlSanitizer.sanitize("<img src=x onerror=alert(1)>내용");

        assertThat(result).doesNotContain("onerror").doesNotContain("<img").contains("내용");
    }

    // 리치 텍스트 에디터가 만들어내는 서식 태그(굵게/기울임/문단)는 허용되어 그대로 남는지 검증
    @Test
    void sanitize_formattingTags_arePreserved() {
        String result = htmlSanitizer.sanitize("<p><b>강조</b>와 <i>기울임</i> 서식</p>");

        assertThat(result).contains("<b>강조</b>").contains("<i>기울임</i>").contains("<p>");
    }

    // 안전한 스킴(https)의 링크는 허용되는지 검증
    @Test
    void sanitize_safeLink_isPreserved() {
        String result = htmlSanitizer.sanitize("<a href=\"https://example.com\">링크</a>");

        assertThat(result).contains("<a").contains("href=\"https://example.com\"").contains("링크");
    }

    // javascript: 스킴 링크는 위험하므로 href 자체가 제거되는지 검증
    @Test
    void sanitize_javascriptSchemeLink_removesHref() {
        String result = htmlSanitizer.sanitize("<a href=\"javascript:alert(1)\">클릭</a>");

        assertThat(result).doesNotContain("javascript:");
    }

    // null 입력은 그대로 null을 반환하는지 검증(값이 있을 때만 처리하는 다른 서비스들과 동일한 방어적 관례)
    @Test
    void sanitize_nullInput_returnsNull() {
        assertThat(htmlSanitizer.sanitize(null)).isNull();
    }
}