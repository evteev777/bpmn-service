package ru.evteev.bpmn.dialogservice.model.dto;

import lombok.Builder;

@Builder
public record MinioVoiceFileInfo(String link,
                                 String fileUniqueId,
                                 Integer duration,
                                 String mimeType,
                                 Long fileSize) {
}
