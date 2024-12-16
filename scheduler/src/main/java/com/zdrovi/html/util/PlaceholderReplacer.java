package com.zdrovi.html.util;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static com.zdrovi.html.util.PlaceholderReplacer.HtmlTemplateKey.*;

@Slf4j
@UtilityClass
public class PlaceholderReplacer {

    @RequiredArgsConstructor
    public enum HtmlTemplateKey {
        HEADER("header"),
        GREETING("greeting"),
        CONTENT("content"),
        SIGNATURE("signature"),
        UNSUBSCRIBE_URL("unsubscribe_url");

        private final String value;

        public String getPlaceholder() {
            return "${" + value + "}";
        }
    }

    @Builder
    @Getter
    public static final class HtmlTemplateValues {
        private String header;
        private String greeting;
        private String content;
        private String signature;
        private String unsubscribeUrl;
    }

    public String replace(String html, final HtmlTemplateValues htmlTemplateValues) {
        String result = html.replace(HEADER.getPlaceholder(), htmlTemplateValues.getHeader());
        result = result.replace(GREETING.getPlaceholder(), htmlTemplateValues.getGreeting());
        result = result.replace(CONTENT.getPlaceholder(), htmlTemplateValues.getContent());
        result = result.replace(SIGNATURE.getPlaceholder(), htmlTemplateValues.getSignature());
        return result.replace(UNSUBSCRIBE_URL.getPlaceholder(), htmlTemplateValues.getUnsubscribeUrl());
    }
}
