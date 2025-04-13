package ru.evteev.bpmn.voicetotextservice.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vosk.Model;
import ru.evteev.bpmn.voicetotextservice.configuration.properties.VoskProperties;
import ru.evteev.bpmn.voicetotextservice.service.AudioConverter;
import ru.evteev.bpmn.voicetotextservice.service.VoiceToTextConverner;
import ru.evteev.bpmn.voicetotextservice.service.impl.FfmpegStreamingAudioConverter;
import ru.evteev.bpmn.voicetotextservice.service.impl.VoskVoiceToTextConverner;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class VoiceToTextConfig {

    private final VoskProperties voskProperties;

    @Bean
    public Model voskModel() throws IOException {
        return new Model(voskProperties.getPathToModel());
    }

    @Bean
    public AudioConverter audioConverter() {
        return new FfmpegStreamingAudioConverter();
    }

    // TODO Не передавать модель в интерфейс (для подмены реализации)
    @Bean
    public VoiceToTextConverner voiceToTextConverner(AudioConverter audioConverter, Model model) {
        return new VoskVoiceToTextConverner(audioConverter, model);
    }
}
