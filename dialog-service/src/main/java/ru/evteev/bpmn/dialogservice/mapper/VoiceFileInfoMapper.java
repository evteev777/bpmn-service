package ru.evteev.bpmn.dialogservice.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Voice;
import ru.evteev.bpmn.dialogservice.model.dto.MinioVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.MultipartVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.TelegramVoiceFileInfo;

import java.util.UUID;

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

    public MultipartVoiceFileInfo toMultipartVoiceFileInfo(MultipartFile file) {
        return MultipartVoiceFileInfo.builder()
            .fileUniqueId(UUID.randomUUID().toString())
            .mimeType(file.getContentType())
            .fileSize(file.getSize())
            .build();
    }

    public MinioVoiceFileInfo toMinioFileInfo(TelegramVoiceFileInfo telegramFile, String minioPublicLink) {
        return MinioVoiceFileInfo.builder()
            .link(minioPublicLink)
            .fileUniqueId(telegramFile.fileUniqueId())
            .fileSize(telegramFile.fileSize())
            .duration(telegramFile.duration())
            .mimeType(telegramFile.mimeType())
            .build();
    }

    public MinioVoiceFileInfo toMinioFileInfo(MultipartVoiceFileInfo telegramFile, String minioPublicLink) {
        return MinioVoiceFileInfo.builder()
            .link(minioPublicLink)
            .fileUniqueId(telegramFile.fileUniqueId())
            .fileSize(telegramFile.fileSize())
            .mimeType(telegramFile.mimeType())
            .build();
    }
}
