package ru.evteev.bpmn.telegramservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evteev.bpmn.telegramservice.client.ProcessManagerClient;
import ru.evteev.bpmn.telegramservice.client.TelegramClient;
import ru.evteev.bpmn.telegramservice.configuration.properties.TelegramBotProperties;
import ru.evteev.bpmn.telegramservice.mapper.VoiceFileInfoMapper;
import ru.evteev.bpmn.telegramservice.model.dto.MinioVoiceFileInfo;
import ru.evteev.bpmn.telegramservice.model.dto.TelegramVoiceFileInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceFileService {

    private final VoiceFileInfoMapper mapper;
    private final TelegramClient telegramClient;
    private final TelegramBotProperties botProperties;
    private final MinioService minioService;
    private final MessageFormatter messageFormatter;
    private final ProcessManagerClient processManagerClient;

    public void processVoiceFile(Update update) {
        TelegramVoiceFileInfo telegramFileInfo = mapper.toTelegramVoiceFileInfo(update);
        String telegramFilePath = telegramClient.getFilePath(telegramFileInfo.getFileId());

        String telegramFileUrl = String.format("https://api.telegram.org/file/bot%s/%s",
            botProperties.getToken(), telegramFilePath);

        String minioPublicLink = minioService.uploadVoiceFileAndGetPublicLink(telegramFileUrl, telegramFileInfo.getFileUniqueId(), telegramFileInfo.getMimeType());

        MinioVoiceFileInfo minioFileInfo = mapper.toMinioFileInfo(telegramFileInfo, minioPublicLink);
        log.debug("MinIO file info: {}", minioFileInfo);

        String telegramMessage = messageFormatter.formatVoiceFileInfo(minioFileInfo);
        telegramClient.reply(telegramFileInfo.getChatId(), telegramFileInfo.getMessageId(), telegramMessage);

        processManagerClient.sendVoiceToProcessManager(minioFileInfo);
    }
}
