package ru.evteev.bpmn.dialogservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BpmnRenderServiceClient {

    private final WebClient baseBpmnRenderServiceClient;

    public byte[] renderFromXml(String xml, String format) {
        log.debug("Sending BPMN XML to renderer, format={}", format);
        return baseBpmnRenderServiceClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/render/bpmn-xml")
                .queryParam("format", format)
                .build())
            .contentType(MediaType.APPLICATION_XML)
            .accept(
                MediaType.IMAGE_PNG,
                MediaType.IMAGE_JPEG,
                MediaType.valueOf("image/svg+xml"),
                MediaType.APPLICATION_PDF)
            .bodyValue(xml)
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new RuntimeException("Renderer error: " + body))))
            .bodyToMono(byte[].class)
            .block();
    }

    public byte[] renderFromFile(byte[] bpmnFileBytes, String format) {
        return baseBpmnRenderServiceClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("//render/bpmn-file")
                .queryParam("format", format)
                .build())
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .body(BodyInserters
                .fromMultipartData("file", new ByteArrayResource(bpmnFileBytes) {
                    @Override
                    public String getFilename() {
                        return "diagram.bpmn";
                    }
                }))
            .retrieve()
            .bodyToMono(byte[].class)
            .block();
    }
}
