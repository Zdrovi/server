package com.zdrovi.algorithm.evaluators;

import com.google.common.collect.Comparators;
import com.zdrovi.algorithm.config.AlgorithmConfig;
import com.zdrovi.algorithm.dto.ContentScore;
import com.zdrovi.domain.entity.Content;
import com.zdrovi.domain.entity.ContentLabel;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.entity.UserLabel;
import com.zdrovi.domain.repository.ContentLabelRepository;
import com.zdrovi.domain.repository.UserLabelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Slf4j
@Component
@RequiredArgsConstructor
public class LabelMatchingEvaluator implements Evaluator {

    private final AlgorithmConfig config;

    private final UserLabelRepository userLabelRepository;

    private final ContentLabelRepository contentLabelRepository;

    private final float dotProductScaler = 1 / 10000.0f;

    private List<Pair<UserLabel, ContentLabel>> findMatchingEntities(final User user, final Content content) {

        List<UserLabel> userLabels = userLabelRepository.findAllByUser(user);
        List<ContentLabel> contentLabels = contentLabelRepository.findAllByContent(content);

        userLabels.sort(Comparator.comparing(userLabel -> userLabel.getLabel().getId()));
        contentLabels.sort(Comparator.comparing(contentLabel -> contentLabel.getLabel().getId()));

        ArrayList<Pair<UserLabel, ContentLabel>> matches = new ArrayList<>();

        int userIndex = 0;
        int contentIndex = 0;

        while (userIndex < userLabels.size() && contentIndex < contentLabels.size()) {
            UserLabel userLabel = userLabels.get(userIndex);
            ContentLabel contentLabel = contentLabels.get(contentIndex);
            UUID userLabelLabel = userLabel.getLabel().getId();
            UUID contentLabelLabel = contentLabel.getLabel().getId();


            int comparisonResult = userLabelLabel.compareTo(contentLabelLabel);

            if (comparisonResult == 0) {
                matches.add(Pair.of(userLabel, contentLabel));
                userIndex++;
                contentIndex++;
            }
            else if (comparisonResult < 0) {
                userIndex++;
            }
            else {
                contentIndex++;
            }
        }
        return matches;
    }

    private Float calculateMatching(final User user, final Content content)
    {
        List<Pair<UserLabel, ContentLabel>> matchingEntities = findMatchingEntities(user, content);

        return matchingEntities
                .stream()
                .map(p ->
                        p.getFirst().getMatching().floatValue()
                                * p.getSecond().getMatching().floatValue()
                                * dotProductScaler)
                .reduce(0f, Float::sum);
    }

    @Override
    public List<ContentScore> evaluate(final User user, final List<ContentScore> contentScores) {
        log.debug("Calculating label matching for user {}", user.getId());
        return contentScores
                .stream()
                .peek(cs -> cs.setScore(cs.getScore() + calculateMatching(user, cs.getContent())))
                .filter(cs -> cs.getScore() >= config.getLabelMatchingMin())
                .toList();
    }
}
