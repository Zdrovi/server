package com.zdrovi;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SchedulerApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

}
