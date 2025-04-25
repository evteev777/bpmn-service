package ru.evteev.bpmn.dialogservice.model.dto;

import lombok.Builder;

@Builder
public record ProcessMultipartVoiceFileResponse(boolean success,
                                                String analytics,
                                                String renderLink,
                                                String error) {
}
