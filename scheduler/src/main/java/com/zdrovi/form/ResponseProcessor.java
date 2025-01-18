package com.zdrovi.form;

import com.zdrovi.domain.entity.Label;
import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.entity.UserLabel;
import com.zdrovi.domain.repository.LabelRepository;
import com.zdrovi.domain.repository.UserLabelRepository;
import com.zdrovi.domain.repository.UserRepository;
import com.zdrovi.email.service.EmailSender;
import com.zdrovi.form.config.FormConfig;
import com.zdrovi.form.decoder.Decoder;
import com.zdrovi.form.decoder.DecoderFactory;
import com.zdrovi.form.dto.DecodedResponse;
import com.zdrovi.form.google.GoogleFormsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseProcessor {
    private final FormConfig config;
    private final GoogleFormsService googleFormsService;
    private final DecoderFactory decoderFactory;
    private final EmailSender emailSender;

    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final UserLabelRepository userLabelRepository;

    private ZonedDateTime lastUpdate = ZonedDateTime.of(2000, 1, 1 ,
            0 , 0, 0, 0, ZoneId.systemDefault());

    @Transactional
    public void processResponses() {
        log.info("Processing responses");
        Decoder decoder = decoderFactory.getDecoder();
        ZonedDateTime now = ZonedDateTime.now();
        List<List<String>> raw_answers = googleFormsService.getAnswers(lastUpdate);
        lastUpdate = now;
        log.info("Got {} answers", raw_answers.size());
        List<DecodedResponse> answers = raw_answers
                .stream()
                .map(decoder::decode)
                .flatMap(Optional::stream)
                .filter(decodedResponse -> userRepository
                        .findUserByEmail(decodedResponse.getEmail())
                        .isEmpty())
                .toList();
        log.info("New answers: {}", answers.size());
        addToDatabase(answers);
    }

    private void addToDatabase(List<DecodedResponse> decodedResponses) {
        decodedResponses.forEach(decodedResponse -> {
            User user = createUser(decodedResponse);
            addMatchings(user, decodedResponse);
            emailSender.sendWelcomeMailToUser(user);
        });
    }

    private void addMatchings(User user, DecodedResponse decodedResponse) {

        for (Map.Entry<String, Short> entry : decodedResponse.getLabelMatching().entrySet())
        {
            String labelName = entry.getKey();
            Short matching = entry.getValue();
            Optional<Label> label_opt = labelRepository.findFirstByName(labelName);

            if (label_opt.isEmpty()) {
                if (config.isCreateLabelIfNotExist()) {
                    label_opt = Optional.of(createLabel(labelName));
                }
            }

            if (label_opt.isEmpty()) {
                log.warn("Label {} not found", labelName);
                continue;
            }

            Label label = label_opt.get();
            createUserLabel(user, label, matching);
        }
    }

    private User createUser(DecodedResponse decodedResponse) {
        log.info("Creating user: {}", decodedResponse.getEmail());
        User user = new User();
        user.setName(decodedResponse.getName());
        user.setEmail(decodedResponse.getEmail());
        return userRepository.save(user);
    }

    private Label createLabel(String labelName) {
        log.info("Creating label: {}", labelName);
        Label label = new Label();
        label.setName(labelName);
        return labelRepository.save(label);
    }

    @SuppressWarnings("UnusedReturnValue")
    private UserLabel createUserLabel(User user, Label label, Short matching) {
        log.info("Creating matching: user: {}, label: {}, matching: {}", user.getName(), label.getName(), matching);
        UserLabel userLabel = new UserLabel();
        userLabel.setLabel(label);
        userLabel.setUser(user);
        userLabel.setMatching(matching);
        return userLabelRepository.save(userLabel);
    }
}
