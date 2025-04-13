package ru.evteev.bpmn.voicetotextservice.controller;

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
import ru.evteev.bpmn.voicetotextservice.model.dto.VoiceToTextUrlRequest;
import ru.evteev.bpmn.voicetotextservice.model.dto.VoiceToTextResponse;
import ru.evteev.bpmn.voicetotextservice.service.OggVoiceToTextConverter;

import java.net.URI;
import java.net.URL;

@Slf4j
@RestController
@RequestMapping("/voice-to-text")
@RequiredArgsConstructor
@Tag(name = "Voice To Text", description = "Обработка голосовых сообщений в формате")
public class VoiceToTextController {

    private final OggVoiceToTextConverter oggVoiceToTextConverter;

    @Operation(summary = "Распознать голос в формате .ogg(.oga) по публичной ссылке на файл")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Успешное распознавание"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации / загрузки файла"),
        @ApiResponse(responseCode = "500", description = "Ошибка распознавания")
    })
    @PostMapping("/ogg/file-url")
    public ResponseEntity<VoiceToTextResponse> oggVoiceToTextFromUrl(
        @Parameter(description = "Публичная ссылка на файл в формате .ogg(.oga)")
        @RequestBody VoiceToTextUrlRequest request) {

        try {
            URL url = new URI(request.url()).toURL();
            String text = oggVoiceToTextConverter.convert(url);
            return ResponseEntity.ok(
                new VoiceToTextResponse(text, true, null));
        } catch (Exception e) {
            log.error("Ошибка распознавания файла по ссылке: {}", request.url(), e);
            return ResponseEntity.badRequest().body(
                new VoiceToTextResponse(null, false, e.getMessage()));
        }
    }
}
