package ru.evteev.bpmn.dialogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.dialogservice.model.dto.VoiceToTextResponse;
import ru.evteev.bpmn.dialogservice.model.dto.VoiceToTextUrlRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceToTextClient {

    private final WebClient webClient = WebClient.create();

    @Value("${client.voice-to-text.url}")
    private String voiceToTextUrl;

    public VoiceToTextResponse convertVoiceToTextFromFileUrl(String fileUrl) {
        log.debug("Sending file URL to Voice-To-Text Service: {}", fileUrl);

        return webClient.post()
            .uri(voiceToTextUrl)
            .bodyValue(new VoiceToTextUrlRequest(fileUrl))
            .retrieve()
            .bodyToMono(VoiceToTextResponse.class)
            .doOnError(e -> log.error("Error calling Voice-To-Text Service", e))
            .block();
    }
}

