package com.example.app_be;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class AppBeApplication {
    @jakarta.annotation.PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(AppBeApplication.class, args);
	}

}