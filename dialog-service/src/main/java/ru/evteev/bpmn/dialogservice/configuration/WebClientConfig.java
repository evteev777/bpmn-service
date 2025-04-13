package ru.evteev.bpmn.dialogservice.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.dialogservice.configuration.properties.TelegramBotProperties;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final TelegramBotProperties telegramBotProperties;

    @Bean
    public WebClient baseTelegramClient() {
        return WebClient.builder()
            .baseUrl("https://api.telegram.org/bot" + telegramBotProperties.getToken())
            .build();
    }
}
