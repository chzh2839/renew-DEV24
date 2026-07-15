package com.dev24.bookstore.common.sanitizer;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

// 리뷰 등 사용자 입력 텍스트에 섞여 들어올 수 있는 <script>/이벤트 핸들러 속성을 저장 전에 제거해
// 저장형 XSS(한 번 저장되면 그 글을 보는 모든 사용자가 공격 대상이 되는 유형)를 막는다.

// 리뷰는 리치 텍스트 에디터(굵게/기울임/문단/링크 등 서식 있는 입력)로 작성되므로, 서식용 태그는 허용하고
// 그 외(<script>, 이벤트 핸들러 속성, javascript: 링크 등)만 제거한다.
// 이미지는 본문에 <img>로 끼워 넣는 게 아니라 별도 파일 업로드(MinIO, Phase 5 계획 참고)로 처리하므로
// Sanitizers.IMAGES는 의도적으로 빼뒀다 - 본문에 <img>가 섞여 있어도 태그째 제거된다.
@Component
public class HtmlSanitizer {

    private final PolicyFactory policy = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS);

    public String sanitize(String rawInput) {
        if (rawInput == null) {
            return null;
        }
        return policy.sanitize(rawInput);
    }
}