package com.zdrovi.form.decoder;

import com.zdrovi.form.dto.DecodedResponse;

import java.util.List;
import java.util.Optional;

public interface Decoder {
    public Optional<DecodedResponse> decode(List<String> responses);
}
