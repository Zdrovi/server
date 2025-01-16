package com.zdrovi.form.google;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.zdrovi.form.FormService;
import com.zdrovi.form.config.FormConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleFormsService implements FormService {
    private final GoogleCredentialService googleCredentialService;
    private final FormConfig formConfig;

    private String lastUpdate = "2000-01-01T12:00:00Z";

    public Forms getFormsService() throws IOException {
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
        try {
            Forms formsService = getFormsService();
            Form form = formsService.forms().get(formConfig.getGoogleFormId()).execute();
            ListFormResponsesResponse responses =  formsService
                    .forms()
                    .responses()
                    .list(formConfig.getGoogleFormId())
                    .setFilter("timestamp >= " + lastUpdate)
                    .execute();
            lastUpdate = Instant.now().toString();
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
