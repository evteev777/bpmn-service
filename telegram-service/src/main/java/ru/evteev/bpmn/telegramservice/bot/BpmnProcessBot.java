package ru.evteev.bpmn.telegramservice.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evteev.bpmn.telegramservice.client.TelegramClient;
import ru.evteev.bpmn.telegramservice.configuration.properties.TelegramBotProperties;
import ru.evteev.bpmn.telegramservice.service.VoiceFileService;

@Slf4j
@Component
public class BpmnProcessBot extends TelegramLongPollingBot {

    private final TelegramBotProperties config;
    private final ObjectMapper objectMapper;
    private final VoiceFileService voiceFileService;
    private final TelegramClient telegramClient;


    public BpmnProcessBot(TelegramBotProperties config,
                          ObjectMapper objectMapper,
                          VoiceFileService voiceFileService, TelegramClient telegramClient) {
        super(config.getToken());
        this.config = config;
        this.objectMapper = objectMapper;
        this.voiceFileService = voiceFileService;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @SneakyThrows(JsonProcessingException.class)
    @Override
    public void onUpdateReceived(Update update) {
        log.info("Получен Telegram Update: {}", objectMapper.writeValueAsString(update));
        if (update.hasMessage() && update.getMessage().hasVoice()) {
            voiceFileService.processVoiceFile(update);
        } else {
            Long chatId = update.getMessage().getChatId();
            Integer messageId = update.getMessage().getMessageId();
            telegramClient.reply(chatId, messageId, "Пока только голосовые сообщения");
        }
    }
}
