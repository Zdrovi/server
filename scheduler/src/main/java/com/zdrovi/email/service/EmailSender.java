package com.zdrovi.email.service;

import com.zdrovi.domain.entity.Content;
import com.zdrovi.domain.entity.User;
import com.zdrovi.email.config.EmailConfig;
import com.zdrovi.html.util.PlaceholderReplacer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Slf4j
public class EmailSender {

    private final JavaMailSender javaMailSender;
    private final EmailConfig emailConfig;
    private final String html;
    private final String welcome_html;

    @SneakyThrows
    public EmailSender(JavaMailSender javaMailSender, EmailConfig emailConfig) {
        this.javaMailSender = javaMailSender;
        this.emailConfig = emailConfig;
        html = new String(Objects.requireNonNull(this.getClass().getClassLoader()
                        .getResourceAsStream("email.html"))
                .readAllBytes(), UTF_8);
        welcome_html = new String(Objects.requireNonNull(this.getClass().getClassLoader()
                        .getResourceAsStream("welcome_email.html"))
                .readAllBytes(), UTF_8);
    }

    @SneakyThrows
    public void sendMailToUser(final User user, final Content content) {
        log.info("Sending email for user: {}, with title: {}", user.getName(), content.getTitle());
        javaMailSender.send(mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8.name());
            helper.setFrom(emailConfig.getFrom());
            helper.setTo(user.getEmail());
            helper.setSubject(content.getTitle());
            helper.setText(PlaceholderReplacer.replace(html, PlaceholderReplacer.HtmlTemplateValues.builder()
                    .header(content.getTitle())
                    .greeting(new String("Cześć".getBytes(), UTF_8)  )// Polish greeting
                    .content(content.getMailContent())
                    .signature("Pozdrawiamy")  // Polish signature
                    .unsubscribeUrl("https://zdrovi.com/wypisz-sie")
                    .build()), true);
            log.debug("Sending email with content {}", helper);
        });
    }

    @SneakyThrows
    public void sendWelcomeMailToUser(final User user) {
        log.info("Sending welcome email to user: {}", user.getName());
        javaMailSender.send(mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8.name());
            helper.setFrom(emailConfig.getFrom());
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to Zdrovi");
            helper.setText(PlaceholderReplacer.replace(welcome_html, PlaceholderReplacer.HtmlTemplateValues.builder()
                    .header("Zdrovi")
                    .greeting(new String("Cześć".getBytes(), UTF_8)  )// Polish greeting
                    .content("Zapisałeś się do programu Zdrovi.")
                    .signature("Pozdrawiamy")  // Polish signature
                    .unsubscribeUrl("https://zdrovi.com/wypisz-sie")
                    .build()), true);
            log.debug("Sending welcome email with content {}", helper);
        });
    }
}
