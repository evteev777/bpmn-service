package ru.evteev.bpmn.dialogservice.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.dialogservice.configuration.properties.TelegramBotProperties;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final TelegramBotProperties telegramBotProperties;

    @Value("${client.voice-to-text.url}")
    private String voiceToTextUrl;

    @Value("${client.bpmn-generator.url}")
    private String bpmnGeneratorUrl;

    @Value("${client.bpmn-render-service.url}")
    private String bpmnRenderServiceUrl;

    @Bean
    public WebClient baseTelegramClient() {
        return WebClient.builder()
            .baseUrl("https://api.telegram.org/bot" + telegramBotProperties.getToken())
            .build();
    }

    @Bean
    public WebClient baseVoiceToTextClient() {
        return WebClient.builder()
            .baseUrl(voiceToTextUrl)
            .build();
    }

    @Bean
    public WebClient baseBpmnGeneratorClient() {
        return WebClient.builder()
            .baseUrl(bpmnGeneratorUrl)
            .build();
    }

    @Bean
    public WebClient baseBpmnRenderServiceClient() {
        return WebClient.builder()
            .baseUrl(bpmnRenderServiceUrl)
            .build();
    }
}
