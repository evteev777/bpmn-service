package ru.evteev.bpmn.dialogservice.service;

import org.springframework.stereotype.Component;
import ru.evteev.bpmn.dialogservice.model.dto.MinioVoiceFileInfo;

@Component
public class MessageFormatter {

    public String formatVoiceFileInfo(MinioVoiceFileInfo info) {
        return String.format("""
                Ссылка: %s
                Длительность: %s
                Размер: %s
                Формат: %s
                """,
            info.link(),
            formatDuration(info.duration()),
            formatFileSize(info.fileSize()),
            info.mimeType()
        );
    }

    private String formatDuration(Integer duration) {
        if (duration == null) return "неизвестна";
        return String.format("%d:%02d", duration / 60, duration % 60);
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null) return "неизвестен";
        if (bytes >= 1024 * 1024) return String.format("%.2f МБ", bytes / 1024.0 / 1024.0);
        if (bytes >= 1024) return String.format("%.1f кБ", bytes / 1024.0);
        return bytes + " Б";
    }
}
