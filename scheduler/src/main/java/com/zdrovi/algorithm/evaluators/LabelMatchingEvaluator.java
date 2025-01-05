package com.zdrovi.algorithm.evaluators;

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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LabelMatchingEvaluator implements Evaluator {

    private final AlgorithmConfig config;

    private final UserLabelRepository userLabelRepository;

    private final ContentLabelRepository contentLabelRepository;

    // Assume that lists are sorted
    private List<Pair<UserLabel, ContentLabel>> findMatchingEntities(List<UserLabel> userLabels, List<ContentLabel> contentLabels) {
        var list = new ArrayList<Pair<UserLabel, ContentLabel>>();
        int i = 0, j = 0;
        while (i < userLabels.size() && j < contentLabels.size()) {
            UserLabel userLabel = userLabels.get(i);
            ContentLabel contentLabel = contentLabels.get(j);
            var userLabelLabelUUID = userLabel.getLabel().getId();
            var contentLabelLabelUUID = contentLabel.getLabel().getId();

            if (userLabelLabelUUID.equals(contentLabelLabelUUID)) {
                list.add(Pair.of(userLabel, contentLabel));
                i++;
                j++;
                continue;
            }

            if (userLabelLabelUUID.compareTo(contentLabelLabelUUID) < 0) {
                i++;
            }
            else {
                j++;
            }
        }
        return list;
    }

    private Float calculateMatching(final User user, final Content content)
    {
        Sort sort = Sort.by(Sort.Direction.ASC, "label.id");
        var userLabels = userLabelRepository.findAllByUser(user, sort);
        var contentLabels = contentLabelRepository.findAllByContent(content, sort);

        var matching_entites = findMatchingEntities(userLabels, contentLabels);

        return matching_entites
                .stream()
                .map(p -> p.getFirst().getMatching() * p.getSecond().getMatching())
                .map(Integer::floatValue)
                .reduce(0f, Float::sum);
    }

    // Returns list of Content sorted by desc matching
    @Override
    public List<ContentScore> evaluate(final User user, List<ContentScore> content_scoring) {
        log.debug("Calculating label matching for user {}", user.getId());
        content_scoring.forEach(cs -> cs.update_score(calculateMatching(user, cs.getContent())));
        content_scoring.removeIf(cs -> cs.getScore() < config.getLabel_matching_min());
        return content_scoring;
    }
}
