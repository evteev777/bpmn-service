package ru.evteev.bpmn.voicetotextservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OggVoiceToTextConverter {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
        "audio/ogg",
        "application/ogg"
    );

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        ".oga",
        ".ogg"
    );

    private final VoiceToTextConverner voiceToTextConverner;

    public String convert(URL publicUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) publicUrl.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();

        String contentType = connection.getContentType();
        String path = publicUrl.getPath();

        if (!isSupported(contentType, path)) {
            throw new IllegalArgumentException("Неподдерживаемый формат аудио: " + contentType + " / " + path);
        }
        return voiceToTextConverner.voiceToText(publicUrl);
    }

    private boolean isSupported(String contentType, String path) {
        boolean byType = contentType != null && SUPPORTED_TYPES.contains(contentType);
        boolean byExt = path != null && SUPPORTED_EXTENSIONS.stream().anyMatch(path::endsWith);
        return byType || byExt;
    }
}
