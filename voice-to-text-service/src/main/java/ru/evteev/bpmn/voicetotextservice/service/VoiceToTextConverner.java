package ru.evteev.bpmn.voicetotextservice.service;

import java.io.IOException;
import java.io.InputStream;

public interface VoiceToTextConverner {

    String voiceToText(InputStream pcmStream) throws IOException;
}
