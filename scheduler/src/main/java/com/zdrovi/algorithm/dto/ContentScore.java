package com.zdrovi.algorithm.dto;

import com.zdrovi.domain.entity.Content;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class ContentScore {

    private final Content content;

    @Setter
    private float score = 0.0f;

}
