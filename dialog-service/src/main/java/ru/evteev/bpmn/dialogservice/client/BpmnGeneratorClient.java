package ru.evteev.bpmn.dialogservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.evteev.bpmn.dialogservice.model.dto.BpmnGenerateRequest;
import ru.evteev.bpmn.dialogservice.model.dto.BpmnGenerateResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class BpmnGeneratorClient {

    private final WebClient baseBpmnGeneratorClient;

    public BpmnGenerateResponse generateBpmn(String prompt) {
        log.debug("Sending prompt to BPMN Generator: {}", prompt);
        return baseBpmnGeneratorClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/smart-generate")
                .build())
            .bodyValue(new BpmnGenerateRequest(prompt))
            .retrieve()
            .bodyToMono(BpmnGenerateResponse.class)
            .doOnError(e -> log.error("Error calling BPMN Generator", e))
            .block();
    }
}
