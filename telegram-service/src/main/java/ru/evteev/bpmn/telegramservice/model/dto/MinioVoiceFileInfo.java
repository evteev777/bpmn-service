package ru.evteev.bpmn.telegramservice.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MinioVoiceFileInfo {

    private String link;
    private String fileUniqueId;
    private Integer duration;
    private String mimeType;
    private Long fileSize;
}
