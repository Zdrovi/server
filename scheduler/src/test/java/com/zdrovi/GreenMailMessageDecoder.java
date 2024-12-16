package com.zdrovi;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.util.List;

import static jakarta.mail.util.StreamProvider.EncoderTypes.QUOTED_PRINTABLE_ENCODER;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Utility class for decoding GreenMail messages.
 * GreenMail API returns messages encoded as UTF-8 with `quoted printable`
 */
@UtilityClass
public class GreenMailMessageDecoder {


    public static String decodeContent(final MimeMessage message) {
        String multipartDecodedText = new String(
                getMultipartBytes(message),
                UTF_8);

        return removeMetadataLines(multipartDecodedText);
    }

    @SneakyThrows
    private static byte[] getMultipartBytes(final MimeMessage message) {
        final InputStream inputMultipartStream = ((MimeMultipart) message.getContent()).getBodyPart(0).getInputStream();
        return MimeUtility.decode(inputMultipartStream, QUOTED_PRINTABLE_ENCODER.getEncoder()).readAllBytes();
    }

    private static String removeMetadataLines(String multipartDecodedText) {
        List<String> lines = Lists.newArrayList(Splitter.on('\n').split(multipartDecodedText));
        List<String> result = lines.subList(4, lines.size() - 3);
        return Joiner.on('\n').join(result);
    }

}