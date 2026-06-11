package com.project1.shriganeshaynamah;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project1.shriganeshaynamah.service.ContactAiService;

@SpringBootTest
class ContactAiServiceTest {

    @Autowired
    private ContactAiService contactAiService;

    @Test
    void aiServiceLoadsWithoutApiKey() {
        assertFalse(contactAiService.isAvailable());
    }

    @Test
    void aiReturnsSetupMessageWhenUnavailable() {
        String reply = contactAiService.askAboutContacts(null, "Hello");
        assertTrue(reply.contains("OPENAI_API_KEY"));
    }
}
