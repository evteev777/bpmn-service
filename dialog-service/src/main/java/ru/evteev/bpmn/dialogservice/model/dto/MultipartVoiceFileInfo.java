package ru.evteev.bpmn.dialogservice.model.dto;

import lombok.Builder;

@Builder
public record MultipartVoiceFileInfo(String fileUniqueId,
                                     String mimeType,
                                     Long fileSize) {
}
