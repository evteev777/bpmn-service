package ru.evteev.speechrecognizer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VoiceOggRecognitionService {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
        "audio/ogg",
        "application/ogg"
    );

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        ".oga",
        ".ogg"
    );

    private final SpeechRecognizer speechRecognizer;

    public String recognize(URL publicUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) publicUrl.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();

        String contentType = connection.getContentType();
        String path = publicUrl.getPath();

        if (!isSupported(contentType, path)) {
            throw new IllegalArgumentException("Неподдерживаемый формат аудио: " + contentType + " / " + path);
        }

        try (InputStream input = publicUrl.openStream()) {
            return speechRecognizer.recognize(input);
        }
    }

    private boolean isSupported(String contentType, String path) {
        boolean byType = contentType != null && SUPPORTED_TYPES.contains(contentType);
        boolean byExt = path != null && SUPPORTED_EXTENSIONS.stream().anyMatch(path::endsWith);
        return byType || byExt;
    }
}
