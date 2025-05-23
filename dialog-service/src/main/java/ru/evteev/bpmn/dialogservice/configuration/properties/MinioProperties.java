package ru.evteev.bpmn.dialogservice.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {

    private String extEndpoint;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
}

