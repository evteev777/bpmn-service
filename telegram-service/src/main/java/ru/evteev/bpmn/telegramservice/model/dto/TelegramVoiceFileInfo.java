package ru.evteev.bpmn.telegramservice.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TelegramVoiceFileInfo {

    private String fileId;
    private String fileUniqueId;
    private Integer duration;
    private String mimeType;
    private Long fileSize;
    private Long telegramUserId;
    private String username;
    private Long chatId;
    private Integer messageId;
}
