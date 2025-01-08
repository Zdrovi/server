package com.zdrovi.algorithm.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@Data
@ConfigurationProperties("algorithm")
public class AlgorithmConfig {
    Float labelMatchingMin;
    Integer courseLength;
    String period;
}
