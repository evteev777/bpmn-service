package ru.evteev.speechrecognizer.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vosk.Model;
import ru.evteev.speechrecognizer.configuration.properties.VoskProperties;
import ru.evteev.speechrecognizer.service.AudioConverter;
import ru.evteev.speechrecognizer.service.SpeechRecognizer;
import ru.evteev.speechrecognizer.service.impl.FfmpegStreamingAudioConverter;
import ru.evteev.speechrecognizer.service.impl.VoskSpeechRecognizer;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class SpeechRecognitionConfig {

    private final VoskProperties voskProperties;

    @Bean
    public Model voskModel() throws IOException {
        return new Model(voskProperties.getPathToModel());
    }

    @Bean
    public AudioConverter audioConverter() {
        return new FfmpegStreamingAudioConverter();
    }

    @Bean
    public SpeechRecognizer speechRecognizer(AudioConverter audioConverter, Model model) {
        return new VoskSpeechRecognizer(audioConverter, model);
    }
}
