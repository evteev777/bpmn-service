package ru.evteev.bpmn.telegramservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.telegramservice.model.dto.MinioVoiceFileInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessManagerClient {

    private final WebClient baseProcessManagerClient;

    public void sendVoiceToProcessManager(MinioVoiceFileInfo voiceFileInfo) {
        // TODO Отправить метаданные и ссылку на файл в S3
//        baseProcessManagerClient.post()
//            .uri("/api/audio-upload")
//            .bodyValue(voiceFileInfo)
//            .retrieve()
//            .bodyToMono(Void.class)
//            .doOnError(e -> log.error("Failed to call Process Management Service", e))
//            .subscribe();
        log.debug("Sent voice file info to process-manager: {}", voiceFileInfo.toString());
    }
}

