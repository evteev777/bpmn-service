package ru.evteev.bpmn.telegramservice.mapper;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Voice;
import ru.evteev.bpmn.telegramservice.model.dto.MinioVoiceFileInfo;
import ru.evteev.bpmn.telegramservice.model.dto.TelegramVoiceFileInfo;

@Component
public class VoiceFileInfoMapper {

    public TelegramVoiceFileInfo toTelegramVoiceFileInfo(Update update) {
        Message message = update.getMessage();
        User user = message.getFrom();
        Voice voice = message.getVoice();
        return TelegramVoiceFileInfo.builder()
            .fileId(voice.getFileId())
            .fileUniqueId(voice.getFileUniqueId())
            .duration(voice.getDuration())
            .mimeType(voice.getMimeType())
            .fileSize(voice.getFileSize())
            .telegramUserId(user.getId())
            .username(user.getUserName())
            .chatId(update.getMessage().getChatId())
            .messageId(message.getMessageId())
            .build();
    }

    public MinioVoiceFileInfo toMinioFileInfo(TelegramVoiceFileInfo telegramFile, String minioPublicLink) {
        return MinioVoiceFileInfo.builder()
            .link(minioPublicLink)
            .fileUniqueId(telegramFile.getFileUniqueId())
            .fileSize(telegramFile.getFileSize())
            .duration(telegramFile.getDuration())
            .mimeType(telegramFile.getMimeType())
            .build();
    }
}
