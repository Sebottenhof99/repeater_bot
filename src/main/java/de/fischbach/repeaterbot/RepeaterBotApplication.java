package de.fischbach.repeaterbot;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RepeaterBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepeaterBotApplication.class, args);
    }

    @Bean
    public ApplicationRunner commandLineRunner(TelegramBot bot, BotController botController) {
        return new BotRunner(bot, botController);
    }

}
