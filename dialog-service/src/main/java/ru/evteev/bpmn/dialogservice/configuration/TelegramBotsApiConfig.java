package ru.evteev.bpmn.dialogservice.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.evteev.bpmn.dialogservice.bot.BpmnProcessTelegramBot;

@Configuration
@RequiredArgsConstructor
public class TelegramBotsApiConfig {

    private final BpmnProcessTelegramBot bot;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
