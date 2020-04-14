package com.knikolov.sharearide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BeShareARideApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeShareARideApplication.class, args);
    }

}
