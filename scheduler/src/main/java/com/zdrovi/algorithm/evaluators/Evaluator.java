package com.zdrovi.algorithm.evaluators;

import com.zdrovi.algorithm.dto.ContentScore;
import com.zdrovi.domain.entity.User;

import java.util.List;

public interface Evaluator {
    List<ContentScore> evaluate(User user, List<ContentScore> contentScores);
}
