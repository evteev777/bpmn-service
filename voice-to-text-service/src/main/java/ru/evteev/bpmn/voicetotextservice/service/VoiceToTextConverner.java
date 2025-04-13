package ru.evteev.bpmn.voicetotextservice.service;

import java.io.File;
import java.net.URL;

public interface VoiceToTextConverner {

    String voiceToText(URL url);

    String voiceToText(File file);
}
