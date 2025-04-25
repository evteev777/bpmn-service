package ru.evteev.bpmn.dialogservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.dialogservice.model.dto.VoiceToTextResponse;
import ru.evteev.bpmn.dialogservice.model.dto.VoiceToTextUrlRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceToTextClient {

    private final WebClient baseVoiceToTextClient;

    public VoiceToTextResponse convertVoiceToTextFromFileUrl(String fileUrl) {
        log.debug("Sending file URL to Voice-To-Text Service: {}", fileUrl);
        return baseVoiceToTextClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/voice-to-text/ogg/file-url")
                .build())
            .bodyValue(new VoiceToTextUrlRequest(fileUrl))
            .retrieve()
            .bodyToMono(VoiceToTextResponse.class)
            .doOnError(e -> log.error("Error calling Voice-To-Text Service", e))
            .block();
    }
}
