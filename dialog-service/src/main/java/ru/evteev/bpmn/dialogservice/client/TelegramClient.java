package ru.evteev.bpmn.dialogservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramClient {

    private final WebClient baseTelegramClient;

    public String getFilePath(String fileId) {
        return baseTelegramClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/getFile")
                .queryParam("file_id", fileId)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(FilePathResponse.class)
            .flatMap(response -> Optional.ofNullable(response)
                .map(FilePathResponse::result)
                .map(FilePathResult::filePath)
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new RuntimeException("file_path отсутствует, повтор запроса..."))))
            .retryWhen(Retry.backoff(20, Duration.ofMillis(500))) // Ждем примерно 10 с
            .block();
    }

    public void reply(long chatId, int toMessageId, String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("reply_to_message_id", toMessageId);

        baseTelegramClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/sendMessage")
                .build())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe();
    }

    public void sendPhotoWithCaption(Long chatId,
                                     Integer replyToMessageId,
                                     String caption,
                                     byte[] photoBytes,
                                     String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId.toString());
        body.add("reply_to_message_id", replyToMessageId.toString());
        body.add("caption", caption);
        // Телега ожидает файл в поле "photo"
        body.add("photo", new ByteArrayResource(photoBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        baseTelegramClient.post()
            .uri("/sendPhoto")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class)
                    .flatMap(err -> Mono.error(new RuntimeException("Telegram sendPhoto error: " + err))))
            .bodyToMono(String.class)
            .block();
    }

    public void sendDocumentWithCaption(Long chatId,
                                        Integer replyToMessageId,
                                        String caption,
                                        byte[] documentBytes,
                                        String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId.toString());
        body.add("reply_to_message_id", replyToMessageId.toString());
        body.add("caption", caption);
        body.add("document", new ByteArrayResource(documentBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        baseTelegramClient.post()
            .uri("/sendDocument")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class)
                    .flatMap(err -> Mono.error(new RuntimeException("Telegram sendDocument error: " + err))))
            .bodyToMono(String.class)
            .block();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FilePathResponse(FilePathResult result) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FilePathResult(
        @JsonProperty("file_path") String filePath
    ) {
    }
}
