package ru.evteev.speechrecognizer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.evteev.speechrecognizer.model.dto.VoiceFromUrlRequest;
import ru.evteev.speechrecognizer.model.dto.VoiceRecognitionResponse;
import ru.evteev.speechrecognizer.service.VoiceOggRecognitionService;

import java.net.URI;
import java.net.URL;

@Slf4j
@RestController
@RequestMapping("/recognize")
@RequiredArgsConstructor
@Tag(name = "Speech Recognition", description = "Обработка голосовых сообщений")
public class VoiceRecognitionController {

    private final VoiceOggRecognitionService voiceRecognitionService;

    @Operation(summary = "Распознать голос по публичной ссылке на файл")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Успешное распознавание"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации / загрузки файла"),
        @ApiResponse(responseCode = "500", description = "Ошибка распознавания")
    })
    @PostMapping("/file-url")
    public ResponseEntity<VoiceRecognitionResponse> recognizeFromUrl(
        @Parameter(description = "Публичная ссылка на OGG-файл")
        @RequestBody VoiceFromUrlRequest request) {

        try {
            URL url = new URI(request.getUrl()).toURL();
            String text = voiceRecognitionService.recognize(url);
            return ResponseEntity.ok(
                VoiceRecognitionResponse.builder()
                    .text(text)
                    .success(true)
                    .build()
            );
        } catch (Exception e) {
            log.error("Ошибка распознавания файла по ссылке: {}", request.getUrl(), e);
            return ResponseEntity.badRequest().body(
                VoiceRecognitionResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build()
            );
        }
    }
}
