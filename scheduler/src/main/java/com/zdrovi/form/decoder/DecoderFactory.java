package com.zdrovi.form.decoder;


import com.zdrovi.form.config.FormConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecoderFactory {
    private final FormConfig formConfig;

    private final Decoder_V1 decoder_V1;

    public Decoder getDecoder() {
        String version = formConfig.getDecoderVersion();
        if (version.equals("V1")) {
            log.info("Selected decoder: V1");
            return decoder_V1;
        }
        log.error("Decoder version not supported: {}", version);
        throw new RuntimeException("Unsupported decoder version: " + version);
    }
}
