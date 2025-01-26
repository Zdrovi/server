package com.zdrovi.form.google;

import com.zdrovi.form.FormService;
import com.zdrovi.form.client.GoogleFormsClient;
import com.zdrovi.form.config.FormConfig;
import com.zdrovi.google.model.Answer;
import com.zdrovi.google.model.Form;
import com.zdrovi.google.model.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleFormsService implements FormService {

    private final GoogleCredentialsService googleCredentialsService;

    private final FormConfig formConfig;

    private final GoogleFormsClient googleFormsClient;

    @Override
    public List<List<String>> getAnswers(ZonedDateTime from) {
        try {
            String timestampRFC3339 = from.toInstant().toString();
            log.info("Looking up for answers from {}", timestampRFC3339);

            String accessToken = googleCredentialsService.getAccessToken();
            com.zdrovi.google.model.Form form = googleFormsClient.formsFormsGet(
                    formConfig.getGoogleFormId(),
                    null,
                    accessToken,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ).getBody();

            List<com.zdrovi.google.model.FormResponse> responses = googleFormsClient.formsFormsResponsesList(
                    formConfig.getGoogleFormId(),
                    null,
                    accessToken,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "timestamp >= " + timestampRFC3339,
                    null,
                    null
            ).getBody().getResponses();

            return responses.stream()
                    .map(response -> extractAnswers(form, response))
                    .toList();
        } catch (Exception e) {
            log.error(e.toString());
            return List.of();
        }
    }

    private List<String> extractAnswers(Form form, com.zdrovi.google.model.FormResponse response) {
        Map<String, Answer> answers = response.getAnswers();

        return form.getItems()
                .stream()
                .map(GoogleFormsService::extractAnswerId)
                .flatMap(Optional::stream)
                .filter(answers::containsKey)
                .map(id -> answers.get(id).getTextAnswers().getAnswers().getFirst().getValue())
                .toList();
    }

    static Optional<String> extractAnswerId(Item item) {
        try {
            String questionId = item.getQuestionItem().getQuestion().getQuestionId();
            return Optional.of(questionId);
        } catch (final NullPointerException ignored) {
            return Optional.empty();
        }
    }

}
