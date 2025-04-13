package ru.evteev.bpmn.dialogservice.service;

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
import org.springframework.web.multipart.MultipartFile;
import ru.evteev.bpmn.dialogservice.configuration.properties.MinioProperties;
import ru.evteev.bpmn.dialogservice.model.dto.MultipartVoiceFileInfo;
import ru.evteev.bpmn.dialogservice.model.dto.TelegramVoiceFileInfo;

import java.io.BufferedInputStream;
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

    public static final String TELEGRAM = "telegram/";
    public static final String MULTIPART = "multipart/";

    private final MinioProperties props;
    private MinioClient client;

    @PostConstruct
    public void init() {
        client = MinioClient.builder()
            .endpoint(props.getEndpoint())
            .credentials(props.getAccessKey(), props.getSecretKey())
            .build();
    }

    public String getPublicLink(String telegramFileUrl, TelegramVoiceFileInfo fileInfo) {
        try (InputStream is = new BufferedInputStream(new URI(telegramFileUrl).toURL().openStream())) {

            return uploadVoiceFileAndGetPublicLink(fileInfo.fileUniqueId(), fileInfo.mimeType(), is, TELEGRAM);

        } catch (URISyntaxException e) {
            throw new RuntimeException("Ошибка в URL файла записи голоса из Telegram", e);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка ввода-вывода", e);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException("Ошибка при сохранении файла в MinIO", e);
        }
    }

    public String getPublicLink(MultipartFile file, MultipartVoiceFileInfo fileInfo) {
        try (InputStream is = new BufferedInputStream(file.getInputStream())) {

            return uploadVoiceFileAndGetPublicLink(fileInfo.fileUniqueId(), fileInfo.mimeType(), is, MULTIPART);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка ввода-вывода", e);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException("Ошибка при сохранении файла в MinIO", e);
        }
    }

    private String uploadVoiceFileAndGetPublicLink(String fileName, String mimeType, InputStream is, String from)
        throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException,
        InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String fileType = Arrays.asList(mimeType.split("/")).getLast();
        String fileNameWithType = from + fileName + "." + fileType;

        Path tempFile = Files.createTempFile("voice_", fileType);
        File file = tempFile.toFile();
        try {
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

            String bucket = props.getBucket();
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            UploadObjectArgs args = UploadObjectArgs.builder()
                .bucket(bucket)
                .object(fileNameWithType)
                .filename(file.getAbsolutePath())
                .contentType(mimeType)
                .build();

            client.uploadObject(args);

            return String.format("%s/%s/%s", props.getExtEndpoint(), bucket, fileNameWithType);
        } finally {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("Удаление временного файла {}: не удалось", file.getAbsolutePath());
            }
        }
    }
}
