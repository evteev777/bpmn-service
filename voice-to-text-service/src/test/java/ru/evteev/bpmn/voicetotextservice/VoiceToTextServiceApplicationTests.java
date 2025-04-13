package ru.evteev.bpmn.voicetotextservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.vosk.Model;

@SpringBootTest
class VoiceToTextServiceApplicationTests {

    @MockitoBean
    private Model model;

    @Test
    void contextLoads() {
    }
}
