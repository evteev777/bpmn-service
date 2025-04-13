package ru.evteev.bpmn.dialogservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.evteev.bpmn.dialogservice.model.dto.VoiceToTextResponse;
import ru.evteev.bpmn.dialogservice.service.VoiceFileService;

@Slf4j
@RestController
@RequestMapping("/api/dialog/voice")
@RequiredArgsConstructor
@Tag(
    name = "Диалог голосом с пользователем",
    description = "Преобразование файла голосового сообщения в текст (в планах - в аналитику и визуализацию bpmn)")
public class VoiceDialogController {

    private final VoiceFileService voiceFileService;

    @Operation(
        summary = "Диалог с пользователем: вопрос - файл .ogg, ответ - текст вопроса (в планах - аналитика и визуализация bpmn)",
        description = "Принимает голосовой файл в формате .ogg(.oga), и возвращает распознанный текст",
        responses = {
            @ApiResponse(responseCode = "200", description = "Распознанный текст",
                content = @Content(schema = @Schema(implementation = VoiceToTextResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный формат файла"),
            @ApiResponse(responseCode = "500", description = "Ошибка при обработке файла")
        }
    )
    @PostMapping(value = "/dialog/bpnm/ogg", consumes = "multipart/form-data")
    public VoiceToTextResponse questionOgg(
        @Parameter(description = "Голосовой файл (.ogg/.oga)", required = true,
            content = @Content(mediaType = "multipart/form-data"))
        @RequestParam("file") MultipartFile file
    ) {
        VoiceToTextResponse voiceAsText = voiceFileService.processMultipartVoiceFile(file);
        log.debug("Сообщение: {}", voiceAsText);
        return voiceAsText;
    }
}
