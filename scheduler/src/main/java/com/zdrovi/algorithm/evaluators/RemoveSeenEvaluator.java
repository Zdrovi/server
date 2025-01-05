package com.zdrovi.algorithm.evaluators;

import com.zdrovi.algorithm.dto.ContentScore;
import com.zdrovi.domain.entity.Content;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveSeenEvaluator implements Evaluator {

    final ContentRepository contentRepository;

    @Override
    public List<ContentScore> evaluate(User user, List<ContentScore> content_scoring) {
        final Set<UUID> seen_contents = contentRepository
                .findAllSeenByUser(user.getId())
                .stream()
                .map(Content::getId)
                .collect(Collectors.toSet());
        content_scoring.removeIf(content -> seen_contents.contains(content.getContent().getId()));
        return content_scoring;
    }
}
