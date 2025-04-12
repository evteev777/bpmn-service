package ru.evteev.speechrecognizer.service;

import java.io.IOException;
import java.io.InputStream;

public interface SpeechRecognizer {

    String recognize(InputStream pcmStream) throws IOException;
}
