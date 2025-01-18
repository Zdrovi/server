package com.zdrovi.form.decoder;

import com.zdrovi.form.dto.DecodedResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@NoArgsConstructor
@Slf4j
public class Decoder_V1 implements Decoder {

    static final private Float matchingScaler = 10f;

    @Override
    public Optional<DecodedResponse> decode(List<String> responses) {
        log.debug("Decoding responses: {}", responses);
        if (responses.size() != 10) {
            log.debug("Invalid number of responses: {}", responses.size());
            return Optional.empty();
        }
        Map<String, Short> label_matching = new HashMap<>();

        String name = responses.get(0);
        String email = responses.get(1);

        label_matching.put("sleep_not_enough", scaleMatching(responses.get(2)));
        label_matching.put("sleep_low_quality", scaleMatching(responses.get(3)));
        label_matching.put("sleep_frequent_wakeups", scaleMatching(responses.get(4)));
        label_matching.put("stress_work", scaleMatching(responses.get(5)));
        label_matching.put("stress_exhaustion", scaleMatching(responses.get(6)));
        label_matching.put("food_obesity", scaleMatching(responses.get(7)));
        label_matching.put("food_anorexia", scaleMatching(responses.get(8)));
        label_matching.put("food_stimulants", scaleMatching(responses.get(9)));

        DecodedResponse decodedResponses = new DecodedResponse(name, email, label_matching);
        log.debug("Decoded responses: {}", decodedResponses);
        return Optional.of(decodedResponses);
    }

    static private short scaleMatching(String m) {
        float matching = matchingScaler * Float.parseFloat(m);
        return (short) Math.clamp(matching, 0.0f, 100.0f);
    }
}
