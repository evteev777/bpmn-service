package ru.evteev.speechrecognizer.service.impl;

import lombok.extern.slf4j.Slf4j;
import ru.evteev.speechrecognizer.service.AudioConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class FfmpegStreamingAudioConverter implements AudioConverter {

    @Override
    public InputStream convertToPcm(File file) throws IOException {
        return convertToPcm(new FileInputStream(file));
    }

    @Override
    public InputStream convertToPcm(InputStream input) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-i", "pipe:0",       // входной поток
            "-ar", "16000",       // частота
            "-ac", "1",           // моно
            "-f", "wav",          // формат
            "pipe:1"              // выходной поток
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Подаём аудио в stdin ffmpeg
        OutputStream ffmpegStdin = process.getOutputStream();
        new Thread(() -> {
            try (input; ffmpegStdin) {
                input.transferTo(ffmpegStdin);
            } catch (IOException e) {
                log.warn("Ошибка при передаче данных в ffmpeg", e);
            }
        }).start();
        // Возвращаем stdout как результат
        return process.getInputStream();
    }
}

