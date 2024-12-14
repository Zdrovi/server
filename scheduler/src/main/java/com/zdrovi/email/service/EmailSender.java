package com.zdrovi.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;

    @Scheduled(cron = "${email.send.period}")
    void sendMail() {
        log.info("Sending email ...");
        var message = new SimpleMailMessage();
        message.setFrom("info.zdrovi@gmail.com");
        message.setTo("wggajda@gmail.com");
        message.setSubject("Wiadomosc od Maciusia");
        message.setText("Macius jest slodki");
        log.info("Sending email {}", message);
        javaMailSender.send(message);
    }
}
