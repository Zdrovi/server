package com.zdrovi.algorithm.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@Data
@ConfigurationProperties("algorithm")
public class AlgorithmConfig {
    private Float labelMatchingMin;
    private Integer courseLength;
    private String period;
}
