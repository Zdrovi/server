package com.zdrovi.form.google;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.Forms.FormsOperations.Responses;
import com.google.api.services.forms.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.zdrovi.form.FormService;
import com.zdrovi.form.config.FormConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleFormsService implements FormService {

    private final GoogleCredentialService googleCredentialService;
    private final FormConfig formConfig;

    @Override
    public List<List<String>> getAnswers(ZonedDateTime from) {
        return getAnswers(Optional.of(from));
    }

    private List<List<String>> getAnswers(Optional<ZonedDateTime> from) {
        try {
            Forms formsService = getFormsService();
            Form form = formsService.forms().get(formConfig.getGoogleFormId()).execute();
            Responses.List responseList = formsService
                    .forms()
                    .responses()
                    .list(formConfig.getGoogleFormId());
            if (from.isPresent()) {
                String timestampRFC3339 = from.get().toInstant().toString();
                log.debug("Looking up for answers from {}", timestampRFC3339);
                responseList = responseList.setFilter("timestamp >= " + timestampRFC3339);
            }
            ListFormResponsesResponse responses = responseList.execute();
            return responses.getResponses().stream()
                    .map(response -> extractAnswers(form, response))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private Forms getFormsService() throws IOException {
        log.debug("Asking for forms service");
        return new Forms.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(googleCredentialService.getCredential())
        )
                .setApplicationName(formConfig.getGoogleApplicationName())
                .build();
    }

    private List<String> extractAnswers(Form form, FormResponse response) {
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
