package com.zdrovi.html;

import java.util.HashMap;
import java.util.Map;

public class HtmlBuilder {
    private StringBuilder html = new StringBuilder();
    protected Map<String, String> mapping;

    public HtmlBuilder(String html) {
        this.html.append(html);
        this.mapping = new HashMap<String,String>();
    }

    public void setCustom(String key, String value) {
        this.mapping.put(key, value);
    }

    public void clear() {
        this.mapping.clear();
    }

    public String build() {
        for (Map.Entry<String, String> entry : this.mapping.entrySet()) {
            int index = this.html.indexOf(entry.getKey());
            while (index != -1) {
                this.html.replace(index, index + entry.getKey().length(), entry.getValue());
                index += entry.getValue().length();
                index = this.html.indexOf(entry.getKey(), index);
            }
        }
        return html.toString();
    }
}
