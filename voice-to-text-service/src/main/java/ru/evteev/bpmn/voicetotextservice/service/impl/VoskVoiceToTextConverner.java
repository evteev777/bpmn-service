package ru.evteev.bpmn.voicetotextservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.vosk.Model;
import org.vosk.Recognizer;
import ru.evteev.bpmn.voicetotextservice.service.AudioConverter;
import ru.evteev.bpmn.voicetotextservice.service.FileDownloader;
import ru.evteev.bpmn.voicetotextservice.service.VoiceToTextConverner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VoskVoiceToTextConverner implements VoiceToTextConverner {

    private final AudioConverter audioConverter;
    private final Model model;

    @Override
    public String voiceToText(URL url) {
        File tempOgg;
        try {
            tempOgg = FileDownloader.downloadToTempFile(url.toString(), "voice_", ".ogg");
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String voiceToText = voiceToText(tempOgg);
        boolean deleted = tempOgg.delete();
        if (!deleted) {
            log.warn("Удаление временного WAV-файла {} не удалось", tempOgg.getAbsolutePath());
        }
        return voiceToText;
    }

    @Override
    public String voiceToText(File file) {
        try (InputStream pcm = new BufferedInputStream(audioConverter.convertToPcm(file))) {
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            byte[] buffer = new byte[4096];
            int bytesRead;
            List<String> recognizedPhrases = new ArrayList<>();

            while ((bytesRead = pcm.read(buffer)) >= 0) {
                boolean accepted = recognizer.acceptWaveForm(buffer, bytesRead);
                if (accepted) {
                    String resultJson = recognizer.getResult();

                    String text = extractTextFromResult(resultJson);
                    if (!text.isBlank()) {
                        recognizedPhrases.add(text);
                    }
                } else {
                    recognizer.getPartialResult();
                }
            }
            String finalResultJson = recognizer.getFinalResult();
            String finalText = extractTextFromResult(finalResultJson);
            if (!finalText.isBlank()) {
                recognizedPhrases.add(finalText);
            }
            String fullText = String.join(" ", recognizedPhrases).trim();
            log.info("Распознанный текст: '{}'", fullText);
            return fullText;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при распознавании речи", e);
        }
    }

    private String extractTextFromResult(String resultJson) {
        try {
            return new ObjectMapper()
                .readTree(resultJson)
                .path("text")
                .asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка парсинга JSON", e);
        }
    }
}
