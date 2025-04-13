package ru.evteev.bpmn.voicetotextservice.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileDownloader {
    public static File downloadToTempFile(String fileUrl, String prefix, String suffix) throws URISyntaxException, MalformedURLException {

        URL url = new URI(fileUrl).toURL();
        try (InputStream is = new BufferedInputStream(url.openStream())) {
            Path tempFile = Files.createTempFile(prefix, suffix);
            Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tempFile.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
