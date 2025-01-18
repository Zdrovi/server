package com.zdrovi.form.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DecodedResponse {
    private String name;
    private String email;
    private Map<String, Short> labelMatching;
}
