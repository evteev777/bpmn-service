package ru.evteev.bpmn.voicetotextservice.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface AudioConverter {

    InputStream convertToPcm(File sourceFile) throws IOException;

    InputStream convertToPcm(InputStream input) throws IOException;
}
