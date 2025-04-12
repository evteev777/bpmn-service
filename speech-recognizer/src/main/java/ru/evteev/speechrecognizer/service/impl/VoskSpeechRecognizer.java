package ru.evteev.speechrecognizer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.vosk.Model;
import org.vosk.Recognizer;
import ru.evteev.speechrecognizer.service.AudioConverter;
import ru.evteev.speechrecognizer.service.SpeechRecognizer;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class VoskSpeechRecognizer implements SpeechRecognizer {

    private final AudioConverter audioConverter;
    private final Model model;

    @Override
    public String recognize(InputStream audioStream) throws IOException {
        try (InputStream pcm = audioConverter.convertToPcm(audioStream)) {
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = pcm.read(buffer)) >= 0) {
                boolean accepted = recognizer.acceptWaveForm(buffer, bytesRead);
                log.debug("Partial: {}", accepted ? recognizer.getResult() : recognizer.getPartialResult());
            }
            return extractTextFromResult(recognizer.getFinalResult());
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
