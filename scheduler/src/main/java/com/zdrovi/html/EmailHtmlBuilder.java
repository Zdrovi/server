package com.zdrovi.html;

public class EmailHtmlBuilder extends HtmlBuilder {
    public EmailHtmlBuilder(String html) {
        super(html);
    }

    public void setHeader(String header) {
        this.mapping.put("header", header);
    }

    public void setGreeting(String greeting) {
        this.mapping.put("greeting", greeting);
    }

    public void setContent(String content) {
        this.mapping.put("content", content);
    }

    public void setSignature(String signature) {
        this.mapping.put("signature", signature);
    }

    public void setUnsubscribeURL(String unsubscribeURL) {
        this.mapping.put("unsubscribe_url", unsubscribeURL);
    }
}