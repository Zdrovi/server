package com.zdrovi.form;

import com.zdrovi.domain.entity.User;
import com.zdrovi.domain.repository.UserRepository;
import com.zdrovi.form.decoder.Decoder;
import com.zdrovi.form.decoder.DecoderFactory;
import com.zdrovi.form.decoder.Decoder_V1;
import com.zdrovi.form.dto.DecodedResponse;
import com.zdrovi.form.google.GoogleFormsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
public class ResponseProcessor {
    private final GoogleFormsService googleFormsService;
    private final UserRepository userRepository;
    private final DecoderFactory decoderFactory;

    @Transactional
    public void processResponses() {
        Decoder decoder = decoderFactory.getDecoder();
        List<List<String>> raw_answers = googleFormsService.getAnswers();
        List<DecodedResponse> answers = raw_answers
                .stream()
                .map(decoder::decode)
                .flatMap(Optional::stream)
                .filter(decodedResponse -> userRepository
                        .findUserByEmail(decodedResponse.getEmail())
                        .isEmpty())
                .toList();
        addToDatabase(answers);
    }

    private void addToDatabase(List<DecodedResponse> decodedResponses) {
        decodedResponses.forEach(decodedResponse -> {
            User user = createUser(decodedResponse);
            addMatchings(user, decodedResponse);
        });
    }

    private User createUser(DecodedResponse decodedResponse) {
        User user = new User();
        user.setName(decodedResponse.getName());
        user.setEmail(decodedResponse.getEmail());
        return userRepository.save(user);
    }

    private void addMatchings(User user, DecodedResponse decodedResponse) {
        decodedResponse.getLabel_matching().forEach( (labelName, matching) ->
        {

        });
    }
}
