package com.zdrovi.form.decoder;


import com.zdrovi.form.config.FormConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DecoderFactory {
    private final FormConfig formConfig;

    private final Decoder_V1 decoder_V1;

    public Decoder getDecoder() {
        String version = formConfig.getDecoderVersion();
        if (version.equals("V1")) {
            return decoder_V1;
        }
        throw new RuntimeException("Unsupported decoder version: " + version);
    }
}
