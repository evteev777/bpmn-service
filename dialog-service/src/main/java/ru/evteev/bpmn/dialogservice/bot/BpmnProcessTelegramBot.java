package ru.evteev.bpmn.dialogservice.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evteev.bpmn.dialogservice.client.TelegramClient;
import ru.evteev.bpmn.dialogservice.configuration.properties.TelegramBotProperties;
import ru.evteev.bpmn.dialogservice.service.VoiceFileService;

// TODO Переделать на вебхуки
@Slf4j
@Component
public class BpmnProcessTelegramBot extends TelegramLongPollingBot {

    private final TelegramBotProperties config;
    private final ObjectMapper objectMapper;
    private final VoiceFileService voiceFileService;
    private final TelegramClient telegramClient;


    public BpmnProcessTelegramBot(TelegramBotProperties config,
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
