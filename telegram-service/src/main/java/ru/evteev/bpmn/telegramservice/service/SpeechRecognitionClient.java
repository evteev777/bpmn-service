package ru.evteev.bpmn.telegramservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.telegramservice.model.dto.SpeechRecognizeUrlRequest;
import ru.evteev.bpmn.telegramservice.model.dto.VoiceRecognitionResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpeechRecognitionClient {

    private final WebClient webClient = WebClient.create();

    @Value("${recognizer.url:http://bpnm.evteev.ru:3002/recognize/file-url}")
    private String recognizeEndpoint;

    public VoiceRecognitionResponse recognizeByUrl(String fileUrl) {
        log.debug("Sending file URL to recognizer: {}", fileUrl);

        return webClient.post()
            .uri(recognizeEndpoint)
            .bodyValue(new SpeechRecognizeUrlRequest(fileUrl))
            .retrieve()
            .bodyToMono(VoiceRecognitionResponse.class)
            .doOnError(e -> log.error("Error calling Speech Recognizer", e))
            .block();
    }
}

