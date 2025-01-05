package com.zdrovi.algorithm.dto;

import com.zdrovi.domain.entity.Content;
import lombok.Getter;

@Getter
public class ContentScore {

    private final Content content;

    private float score;

    public ContentScore(Content content)
    {
        this.content = content;
        this.score = 0.0f;
    }

    public void update_score(float modifier)
    {
        this.score += modifier;
    }
}