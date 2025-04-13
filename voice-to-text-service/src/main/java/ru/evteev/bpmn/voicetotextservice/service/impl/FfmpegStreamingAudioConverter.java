package ru.evteev.bpmn.voicetotextservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import ru.evteev.bpmn.voicetotextservice.service.AudioConverter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

@Slf4j
public class FfmpegStreamingAudioConverter implements AudioConverter {

    @Override
    public InputStream convertToPcm(File file) throws IOException {
        File tempWav = File.createTempFile("converted_", ".wav");
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-y",                       // перезапись
            "-i", "pipe:0",             // stdin
            "-ar", "16000",             // частота
            "-ac", "1",                 // моно
            "-f", "wav",                // формат
            tempWav.getAbsolutePath()   // вывод
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Отдельный поток записи в stdin ffmpeg
        new Thread(() -> {
            try (InputStream is = new BufferedInputStream(new FileInputStream(file));
                 OutputStream ffmpegStdin = process.getOutputStream()) {
                is.transferTo(ffmpegStdin);
            } catch (IOException e) {
                log.warn("Ошибка при передаче данных в ffmpeg", e);
            }
        }).start();

        // Поток чтения stderr+stdout ffmpeg
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

                while (reader.readLine() != null) {
                    // читаем, пока не прочитаем все
                }
            } catch (IOException e) {
                log.warn("Ошибка чтения вывода ffmpeg", e);
            }
        }).start();

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("ffmpeg завершился с кодом: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Ожидание завершения ffmpeg прервано", e);
        }
        // Возвращаем InputStream, который удалит файл после чтения
        return new FilterInputStream(new FileInputStream(tempWav)) {
            @Override
            public void close() throws IOException {
                super.close();
                boolean deleted = tempWav.delete();
                if (!deleted) {
                    log.warn("Удаление временного WAV-файла {} не удалось", tempWav.getAbsolutePath());
                }
            }
        };
    }
}
