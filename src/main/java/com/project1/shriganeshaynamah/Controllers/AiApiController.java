package com.project1.shriganeshaynamah.Controllers;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.dto.AiChatRequest;
import com.project1.shriganeshaynamah.dto.ApiResponse;
import com.project1.shriganeshaynamah.service.ContactAiService;
import com.project1.shriganeshaynamah.service.ContactService;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

@RestController
@RequestMapping("/api/ai")
public class AiApiController {

    @Autowired
    private userDao userRepository;

    @Autowired
    private ContactAiService contactAiService;

    @Autowired
    private ContactService contactService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "enabled", contactAiService.isAvailable(),
                "provider", contactAiService.isAvailable() ? "openai" : "none"
        )));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(
            @RequestBody AiChatRequest request,
            Principal principal) {

        User user = userRepository.findByName(principal.getName());

        try {
            String reply = contactAiService.askAboutContacts(user, request.getMessage());
            return ResponseEntity.ok(ApiResponse.ok(Map.of("reply", reply)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, String>>> summary(Principal principal) {
        User user = userRepository.findByName(principal.getName());
        String summary = contactAiService.summarizeContacts(user);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("summary", summary)));
    }

    @PostMapping("/contacts/{id}/notes")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateNotes(
            @PathVariable int id,
            Principal principal) {

        User user = userRepository.findByName(principal.getName());
        Contact contact = contactService.requireOwnedContact(id, user);
        String notes = contactAiService.generateContactNotes(contact);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("notes", notes)));
    }
}
