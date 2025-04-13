package ru.evteev.bpmn.dialogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evteev.bpmn.dialogservice.client.TelegramClient;
import ru.evteev.bpmn.dialogservice.client.VoiceToTextClient;
import ru.evteev.bpmn.dialogservice.configuration.properties.TelegramBotProperties;
import ru.evteev.bpmn.dialogservice.mapper.VoiceFileInfoMapper;
import ru.evteev.bpmn.dialogservice.model.dto.MinioVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.MultipartVoiceFileInfo;
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

    public void processTelegramVoiceFile(Update update) {
        TelegramVoiceFileInfo telegramFileInfo = mapper.toTelegramVoiceFileInfo(update);
        String telegramFilePath = telegramClient.getFilePath(telegramFileInfo.fileId());
        String telegramFileUrl = String.format("https://api.telegram.org/file/bot%s/%s",
            botProperties.getToken(), telegramFilePath);

        String minioPublicLink = minioService.getPublicLink(telegramFileUrl, telegramFileInfo);
        MinioVoiceFileInfo minioFileInfo = mapper.toMinioFileInfo(telegramFileInfo, minioPublicLink);
        log.debug("MinIO file info: {}", minioFileInfo);

        VoiceToTextResponse voiceToText = voiceToTextClient.convertVoiceToTextFromFileUrl(minioPublicLink);

        Long chatId = telegramFileInfo.chatId();
        Integer messageId = telegramFileInfo.messageId();
        String voiceAsTextResult = voiceToText.success() ? voiceToText.text() : voiceToText.error();
        String telegramMessage = messageFormatter.formatVoiceFileInfo(minioFileInfo, voiceAsTextResult);
        log.debug("Telegram message: {}", telegramMessage);
        telegramClient.reply(chatId, messageId, telegramMessage);
    }

    public VoiceToTextResponse processMultipartVoiceFile(MultipartFile file) {
        MultipartVoiceFileInfo multipartFileInfo = mapper.toMultipartVoiceFileInfo(file);

        String minioPublicLink = minioService.getPublicLink(file, multipartFileInfo);
        MinioVoiceFileInfo minioFileInfo = mapper.toMinioFileInfo(multipartFileInfo, minioPublicLink);
        log.debug("MinIO file info: {}", minioFileInfo);

        return voiceToTextClient.convertVoiceToTextFromFileUrl(minioPublicLink);
    }
}
