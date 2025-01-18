package com.zdrovi.form.google;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.Forms.FormsOperations.Responses;
import com.google.api.services.forms.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.zdrovi.form.FormService;
import com.zdrovi.form.config.FormConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
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

    public Forms getFormsService() throws IOException {
        log.debug("Asking for forms service");
        GoogleCredentials credential = googleCredentialService.getCredential();
        return new Forms.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credential)
        )
                .setApplicationName(formConfig.getGoogleApplicationName())
                .build();
    }

    @Override
    public List<List<String>> getAnswers() {
        return getAnswers(Optional.empty());
    }

    @Override
    public List<List<String>> getAnswers(ZonedDateTime from) {
        return getAnswers(Optional.of(from));
    }

    public List<List<String>> getAnswers(Optional<ZonedDateTime> from) {
        try {
            Forms formsService = getFormsService();
            Form form = formsService.forms().get(formConfig.getGoogleFormId()).execute();
            Responses.List response_list = formsService
                    .forms()
                    .responses()
                    .list(formConfig.getGoogleFormId());
            if (from.isPresent()) {
                String timestampRFC3339 = from.get().toInstant().toString();
                log.debug("Looking up for answers from {}", timestampRFC3339);
                response_list = response_list.setFilter("timestamp >= " + timestampRFC3339);
            }
            ListFormResponsesResponse responses = response_list.execute();
            return responses.getResponses().stream().map(response -> extract_answers(form, response)).toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    static Optional<String> extract_answer_id(Item item) {
        try {
            String question_id = item.getQuestionItem().getQuestion().getQuestionId();
            return Optional.of(question_id);
        }
        catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private List<String> extract_answers(Form form, FormResponse response) {
        var question_ids = form.getItems()
                .stream()
                .map(GoogleFormsService::extract_answer_id)
                .flatMap(Optional::stream)
                .toList();
        Map<String, Answer> answers = response.getAnswers();
        return question_ids
                .stream()
                .filter(answers::containsKey)
                .map(id -> answers.get(id).getTextAnswers().getAnswers().getFirst().getValue())
                .toList();
    }
}
