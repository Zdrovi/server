package com.zdrovi;

import com.zdrovi.email.config.EmailConfig;
import com.zdrovi.user.config.UserConfig;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories
@EnableConfigurationProperties({
        EmailConfig.class,
        UserConfig.class
})
public class SchedulerApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

}
