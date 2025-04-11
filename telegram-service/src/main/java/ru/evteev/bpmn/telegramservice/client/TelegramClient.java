package ru.evteev.bpmn.telegramservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FilePathResponse(FilePathResult result) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FilePathResult(
        @JsonProperty("file_path") String filePath
    ) {
    }
}
