package com.project1.shriganeshaynamah.Controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.service.ContactAiService;
import com.project1.shriganeshaynamah.service.ContactService;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class AiController {

    @Autowired
    private userDao userRepository;

    @Autowired
    private ContactAiService contactAiService;

    @Autowired
    private ContactService contactService;

    @ModelAttribute
    public void addLoggedInUser(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByName(principal.getName()));
        }
    }

    @GetMapping("/user/ai")
    public String assistantPage(Model model) {
        model.addAttribute("pageTitle", "AI Assistant");
        model.addAttribute("activePage", "ai");
        model.addAttribute("aiEnabled", contactAiService.isAvailable());
        return "user/ai-assistant";
    }

    @PostMapping("/user/ai/generate-notes")
    public String generateNotes(
            @RequestParam int contactId,
            Principal principal,
            HttpSession session) {

        User user = userRepository.findByName(principal.getName());
        Contact contact = contactService.requireOwnedContact(contactId, user);

        try {
            String notes = contactAiService.generateContactNotes(contact);
            session.setAttribute("aiGeneratedNotes", notes);
        } catch (IllegalArgumentException ex) {
            session.setAttribute("aiError", ex.getMessage());
        }

        return "redirect:/user/" + contactId + "/ind";
    }
}
