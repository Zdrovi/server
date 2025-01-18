package com.zdrovi.form.decoder;

import com.zdrovi.form.dto.DecodedResponse;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@NoArgsConstructor
public class Decoder_V1 implements Decoder {

    static final private Float matchingScaler = 10f;

    @Override
    public Optional<DecodedResponse> decode(List<String> responses) {
        if (responses.size() != 11) {
            return Optional.empty();
        }
        Map<String, Short> label_matching = new HashMap<>();

        String name = responses.get(0);
        String email = responses.get(1);

        label_matching.put("a", scaleMatching(responses.get(2)));
        label_matching.put("b", scaleMatching(responses.get(2)));
        label_matching.put("c", scaleMatching(responses.get(2)));
        label_matching.put("d", scaleMatching(responses.get(5)));
        label_matching.put("e", scaleMatching(responses.get(6)));
        label_matching.put("f", scaleMatching(responses.get(7)));
        label_matching.put("g", scaleMatching(responses.get(8)));
        label_matching.put("h", scaleMatching(responses.get(9)));
        label_matching.put("i", scaleMatching(responses.get(10)));

        return Optional.of(new DecodedResponse(name, email, label_matching));
    }

    static private short scaleMatching(String m) {
        float matching = matchingScaler * Float.parseFloat(m);
        return (short) Math.clamp(matching, 0.0f, 100.0f);
    }
}
