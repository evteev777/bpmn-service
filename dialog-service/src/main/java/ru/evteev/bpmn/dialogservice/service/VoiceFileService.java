package ru.evteev.bpmn.dialogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evteev.bpmn.dialogservice.client.TelegramClient;
import ru.evteev.bpmn.dialogservice.configuration.properties.TelegramBotProperties;
import ru.evteev.bpmn.dialogservice.mapper.VoiceFileInfoMapper;
import ru.evteev.bpmn.dialogservice.model.dto.MinioVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.TelegramVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.VoiceToTextResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceFileService {

    private final VoiceFileInfoMapper mapper;
    private final TelegramClient telegramClient;
    private final TelegramBotProperties botProperties;
    private final MinioService minioService;
    private final MessageFormatter messageFormatter;
    private final VoiceToTextClient voiceToTextClient;

    public void processVoiceFile(Update update) {
        TelegramVoiceFileInfo telegramFileInfo = mapper.toTelegramVoiceFileInfo(update);
        String telegramFilePath = telegramClient.getFilePath(telegramFileInfo.fileId());

        String telegramFileUrl = String.format("https://api.telegram.org/file/bot%s/%s",
            botProperties.getToken(), telegramFilePath);

        String minioPublicLink = minioService.uploadVoiceFileAndGetPublicLink(
            telegramFileUrl, telegramFileInfo.fileUniqueId(), telegramFileInfo.mimeType());

        MinioVoiceFileInfo minioFileInfo = mapper.toMinioFileInfo(telegramFileInfo, minioPublicLink);
        log.debug("MinIO file info: {}", minioFileInfo);

        VoiceToTextResponse voiceToText = voiceToTextClient.convertVoiceToTextFromFileUrl(minioPublicLink);
        String voiceAsText = voiceToText.success() ? voiceToText.text() : voiceToText.error();

        String telegramMessage = messageFormatter.formatVoiceFileInfo(minioFileInfo, voiceAsText);

        Long chatId = telegramFileInfo.chatId();
        Integer messageId = telegramFileInfo.messageId();
        telegramClient.reply(chatId, messageId, telegramMessage);
    }
}
