package de.fischbach.repeaterbot;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RepeaterConfig {

    @Bean(destroyMethod = "shutdown")
    public TelegramBot telegramBot(Environment env) {
        System.out.println("creat telegram bot");
        return new TelegramBot("7724829961:AAHgq3zljIQgdie3_Ao-_OYghFBBuTzhBlU");
    }

    @Bean
    public BotController botController(TelegramBot telegramBot, ExecutorService executorService) {
        return new BotController(telegramBot, executorService);
    }


    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(6);
    }
}
