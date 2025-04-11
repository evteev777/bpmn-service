package ru.evteev.bpmn.telegramservice.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.evteev.bpmn.telegramservice.configuration.properties.MinioProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioProperties props;
    private MinioClient client;

    @PostConstruct
    public void init() {
        client = MinioClient.builder()
            .endpoint(props.getEndpoint())
            .credentials(props.getAccessKey(), props.getSecretKey())
            .build();
    }

    public String uploadVoiceFileAndGetPublicLink(String telegramFileUrl, String fileName, String mimeType) {
        String publicLink;
        try (InputStream in = new URI(telegramFileUrl).toURL().openStream()) {

            String fileType = Arrays.asList(mimeType.split("/")).getLast();
            String fileNameWithType = fileName + "." + fileType;

            Path tempFile = Files.createTempFile("voice_", fileType);
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            publicLink = uploadVoiceFile(tempFile.toFile(), fileNameWithType, mimeType);

        } catch (URISyntaxException e) {
            throw new RuntimeException("Ошибка в URL файла записи голоса из Telegram", e);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка ввода-вывода", e);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException("Ошибка при сохранении файла в MinIO", e);
        }
        return publicLink;
    }

    private String uploadVoiceFile(File file, String fileNameWithType, String mimeType) throws ServerException,
        InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
        InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String bucket = getOrCreateBucket();
        String s3ObjectKey = "voice/" + fileNameWithType;
        UploadObjectArgs args = UploadObjectArgs.builder()
            .bucket(bucket)
            .object(s3ObjectKey)
            .filename(file.getAbsolutePath())
            .contentType(mimeType)
            .build();
        client.uploadObject(args);
        return String.format("%s/%s/%s", props.getExtEndpoint(), bucket, s3ObjectKey);
    }

    private String getOrCreateBucket() throws ServerException,
        InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
        InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String bucket = props.getBucket();
        boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        return bucket;
    }
}
