package ru.evteev.bpmn.dialogservice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceToTextResponse(String text,
                                  boolean success,
                                  String error) {
}
