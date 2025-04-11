package ru.evteev.bpmn.telegramservice.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.evteev.bpmn.telegramservice.bot.BpmnProcessBot;

@Configuration
@RequiredArgsConstructor
public class TelegramBotsApiConfig {

    private final BpmnProcessBot bot;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
