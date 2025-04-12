package ru.evteev.speechrecognizer.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vosk")
@Data
public class VoskProperties {

    private String pathToModel;
}
