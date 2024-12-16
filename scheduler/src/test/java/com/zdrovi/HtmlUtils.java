package com.zdrovi;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HtmlUtils {

    public static boolean verifyHtmlEqual(String html1, String html2) {
        String normalized1 = normalizeHtml(html1);
        String normalized2 = normalizeHtml(html2);
        return normalized1.equals(normalized2);
    }

    private static String normalizeHtml(String html) {
        return html.replaceAll("\\s+", " ")  // Replace multiple spaces with single space
                .replaceAll("\\s*>\\s*", ">")  // Remove spaces around >
                .replaceAll("\\s*<\\s*", "<")  // Remove spaces around
                .replaceAll("\\s+/", "/")  // Remove spaces before /
                .trim();
    }
}
