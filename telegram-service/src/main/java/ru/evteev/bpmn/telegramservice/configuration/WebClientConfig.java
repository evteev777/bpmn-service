package ru.evteev.bpmn.telegramservice.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.telegramservice.configuration.properties.TelegramBotProperties;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final TelegramBotProperties telegramBotProperties;

    @Bean
    public WebClient baseProcessManagerClient(@Value("${client.process-manager.url}") String baseUrl) {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Bean
    public WebClient baseTelegramClient() {
        return WebClient.builder()
            .baseUrl("https://api.telegram.org/bot" + telegramBotProperties.getToken())
            .build();
    }
}
