package com.zdrovi.algorithm.service;

import com.zdrovi.algorithm.config.AlgorithmConfig;
import com.zdrovi.algorithm.dto.ContentScore;
import com.zdrovi.algorithm.evaluators.Evaluator;
import com.zdrovi.algorithm.evaluators.LabelMatchingEvaluator;
import com.zdrovi.algorithm.evaluators.RemoveSeenEvaluator;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseBuilder {

    final private ContentRepository contentRepository;

    final private CourseRepositoryHelper courseRepositoryHelper;

    final private AlgorithmConfig config;

    final private List<Evaluator> evaluators;

    public void prepareCourse(final User user)
    {
        if (courseRepositoryHelper.hasOpenCourse(user)) {
            log.warn("User {} already has open course", user.getId());
            return;
        }

        List<ContentScore> content_scores = contentRepository
                .findAll()
                .stream()
                .map(ContentScore::new)
                .toList();

        for (var evaluator : evaluators) {
            content_scores = evaluator.evaluate(user, content_scores);
        }

        var course_content = content_scores
                .stream()
                .sorted((lhs, rhs) -> Float.compare(rhs.getScore(), lhs.getScore()))
                .limit(config.getCourseLength())
                .map(ContentScore::getContent)
                .toList();

        if (course_content.isEmpty()) {
            log.warn("Can't find any content for user: {}", user.getId());
            return;
        }

        courseRepositoryHelper.createCourseForUser(user,course_content);
    }
}
