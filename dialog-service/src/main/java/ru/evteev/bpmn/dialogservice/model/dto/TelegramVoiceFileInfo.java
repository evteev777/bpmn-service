package ru.evteev.bpmn.dialogservice.model.dto;

import lombok.Builder;

@Builder
public record TelegramVoiceFileInfo(String fileId,
                                    String fileUniqueId,
                                    Integer duration,
                                    String mimeType,
                                    Long fileSize,
                                    Long telegramUserId,
                                    String username,
                                    Long chatId,
                                    Integer messageId) {
}
