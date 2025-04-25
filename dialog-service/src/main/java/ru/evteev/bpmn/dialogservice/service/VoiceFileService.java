package ru.evteev.bpmn.dialogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evteev.bpmn.dialogservice.client.BpmnGeneratorClient;
import ru.evteev.bpmn.dialogservice.client.BpmnRenderServiceClient;
import ru.evteev.bpmn.dialogservice.client.TelegramClient;
import ru.evteev.bpmn.dialogservice.client.VoiceToTextClient;
import ru.evteev.bpmn.dialogservice.configuration.properties.TelegramBotProperties;
import ru.evteev.bpmn.dialogservice.mapper.VoiceFileInfoMapper;
import ru.evteev.bpmn.dialogservice.model.dto.BpmnGenerateResponse;
import ru.evteev.bpmn.dialogservice.model.dto.MinioVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.MultipartVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.ProcessMultipartVoiceFileResponse;
import ru.evteev.bpmn.dialogservice.model.dto.TelegramVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.VoiceToTextResponse;
import ru.evteev.bpmn.dialogservice.model.enums.RenderFileType;

import static ru.evteev.bpmn.dialogservice.model.enums.RenderFileType.JPEG;
import static ru.evteev.bpmn.dialogservice.model.enums.RenderFileType.PDF;
import static ru.evteev.bpmn.dialogservice.model.enums.RenderFileType.PNG;
import static ru.evteev.bpmn.dialogservice.model.enums.RenderFileType.SVG;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceFileService {

    public static final String FILENAME = "diagram";
    public static final String RENDER_CAPITON = "Процесс BPMN";
    private final VoiceFileInfoMapper mapper;
    private final TelegramClient telegramClient;
    private final TelegramBotProperties botProperties;
    private final MinioService minioService;
    private final MessageFormatter messageFormatter;
    private final VoiceToTextClient voiceToTextClient;
    private final BpmnGeneratorClient bpmnGeneratorClient;
    private final BpmnRenderServiceClient bpmnRenderServiceClient;

    @Value("${render.format:png}")
    private String renderFormat;

    public void processTelegramVoiceFile(Update update) {
        TelegramVoiceFileInfo telegramFileInfo = mapper.toTelegramVoiceFileInfo(update);

        Long chatId = telegramFileInfo.chatId();
        Integer messageId = telegramFileInfo.messageId();

        String telegramFilePath = telegramClient.getFilePath(telegramFileInfo.fileId());
        String telegramFileUrl = String.format("https://api.telegram.org/file/bot%s/%s",
            botProperties.getToken(), telegramFilePath);

        String minioPublicLink = minioService.getPublicLink(telegramFileUrl, telegramFileInfo);
        MinioVoiceFileInfo minioFileInfo = mapper.toMinioFileInfo(telegramFileInfo, minioPublicLink);
        log.debug("MinIO file info: {}", minioFileInfo);

        VoiceToTextResponse voiceToText = voiceToTextClient.convertVoiceToTextFromFileUrl(minioPublicLink);

        if (!voiceToText.success()) {
            String errorMsg = voiceToText.error();
            log.debug("Telegram error message: {}", errorMsg);
            telegramClient.reply(chatId, messageId, errorMsg);
        }
        String prompt = voiceToText.text();
        log.debug("Raw prompt: {}", prompt);
        telegramClient.reply(chatId, messageId, "Исходный запрос: \n" + prompt);

        String voiceFileInfoMsg = messageFormatter.formatVoiceFileInfo(minioFileInfo);
        log.debug("Voice file info: {}", voiceFileInfoMsg);
        telegramClient.reply(chatId, messageId, "Запись голоса: \n" + voiceFileInfoMsg);

        BpmnGenerateResponse bpmnGenerateResponse = bpmnGeneratorClient.generateBpmn(prompt);

        String structure = bpmnGenerateResponse.structure();
        log.debug("BPMN structure: {}", structure);
        telegramClient.reply(chatId, messageId, "Структура JSON: \n" + structure);

        String analytics = bpmnGenerateResponse.analytics();
        log.debug("BPMN analytics: {}", analytics);
        telegramClient.reply(chatId, messageId, "Аналитика: \n" + analytics);

        String bpmnXml = bpmnGenerateResponse.bpmn();
        log.debug("BPMN XML: {}", bpmnXml);
        telegramClient.reply(chatId, messageId, "BPMN XML: \n" + bpmnXml);

        String filename = String.format("%s.%s", FILENAME, renderFormat);
        byte[] render = bpmnRenderServiceClient.renderFromXml(bpmnXml, renderFormat);
        sendRenderToTelegram(chatId, messageId, render, filename);
    }

    private void sendRenderToTelegram(Long chatId, Integer messageId, byte[] render, String filename) {
        if (isRenderFormat(JPEG) || isRenderFormat(PNG)) {
            telegramClient.sendPhotoWithCaption(chatId, messageId, RENDER_CAPITON, render, filename);
        }
        if (isRenderFormat(SVG) || isRenderFormat(PDF)) {
            telegramClient.sendDocumentWithCaption(chatId, messageId, RENDER_CAPITON, render, filename);
        } else {
            throw new RuntimeException("Unsupported format: " + renderFormat);
        }
    }

    private boolean isRenderFormat(RenderFileType renderFileType) {
        return renderFileType.name().toLowerCase().equals(renderFormat);
    }

    public ProcessMultipartVoiceFileResponse processMultipartVoiceFile(MultipartFile file) {
        MultipartVoiceFileInfo multipartFileInfo = mapper.toMultipartVoiceFileInfo(file);

        String minioPublicLink = minioService.getPublicLink(file, multipartFileInfo);
        MinioVoiceFileInfo minioFileInfo = mapper.toMinioFileInfo(multipartFileInfo, minioPublicLink);
        log.debug("MinIO file info: {}", minioFileInfo);

        VoiceToTextResponse voiceToText = voiceToTextClient.convertVoiceToTextFromFileUrl(minioPublicLink);
        if (!voiceToText.success()) {
            log.debug("Error message: {}", voiceToText.error());
            return ProcessMultipartVoiceFileResponse.builder()
                .success(false)
                .error(voiceToText.error())
                .build();
        }
        BpmnGenerateResponse bpmnGenerateResponse = bpmnGeneratorClient.generateBpmn(voiceToText.text());
        return ProcessMultipartVoiceFileResponse.builder()
            .success(true)
            .analytics(bpmnGenerateResponse.analytics())
            .renderLink("TODO: Minio render link")
            .build();
    }
}
