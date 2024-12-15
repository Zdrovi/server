package com.zdrovi.html.util;

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

    public record HtmlTemplateValues(String header,
                                     String greeting,
                                     String content,
                                     String signature,
                                     String unsubscribeUrl) {
    }

    public String replace(String html, final HtmlTemplateValues htmlTemplateValues) {
        String result = html.replace(HEADER.getPlaceholder(), htmlTemplateValues.header);
        result = result.replace(GREETING.getPlaceholder(), htmlTemplateValues.greeting);
        result = result.replace(CONTENT.getPlaceholder(), htmlTemplateValues.content);
        result = result.replace(SIGNATURE.getPlaceholder(), htmlTemplateValues.signature);
        return result.replace(UNSUBSCRIBE_URL.getPlaceholder(), htmlTemplateValues.unsubscribeUrl);
    }
}
