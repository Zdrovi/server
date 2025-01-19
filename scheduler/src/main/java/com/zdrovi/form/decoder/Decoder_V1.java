package com.zdrovi.form.decoder;

import com.zdrovi.form.dto.DecodedResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

        var decodedResponses = new DecodedResponse(
                responses.get(0),
                responses.get(1),
                Map.of(
                        "sleep_not_enough", scaleMatching(responses.get(2)),
                        "sleep_low_quality", scaleMatching(responses.get(3)),
                        "sleep_frequent_wakeups", scaleMatching(responses.get(4)),
                        "stress_work", scaleMatching(responses.get(5)),
                        "stress_exhaustion", scaleMatching(responses.get(6)),
                        "food_obesity", scaleMatching(responses.get(7)),
                        "food_anorexia", scaleMatching(responses.get(8)),
                        "food_stimulants", scaleMatching(responses.get(9))
                ));
        log.debug("Decoded responses: {}", decodedResponses);
        return Optional.of(decodedResponses);
    }

    static private short scaleMatching(String m) {
        float matching = matchingScaler * Float.parseFloat(m);
        return (short) Math.clamp(matching, 0.0f, 100.0f);
    }
}
