package com.project1.shriganeshaynamah.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.project1.shriganeshaynamah.Dao.contacc;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

@Service
public class ContactAiService {

    private static final String NOT_CONFIGURED =
            "Spring AI is not configured yet. Add your OpenAI API key as OPENAI_API_KEY and restart the app.";

    private static final String SYSTEM_PROMPT = """
            You are Smart Contact Manager AI, a helpful assistant for a personal contact book.
            Answer only using the contact data provided below.
            If the answer is not in the data, say you do not have that information.
            Be concise, friendly, and practical. Use bullet points when listing contacts.
            Never invent contacts or details that are not in the data.
            """;

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private contacc contactRepository;

    public boolean isAvailable() {
        return chatModel != null;
    }

    public String askAboutContacts(User user, String question) {
        if (chatModel == null) {
            return NOT_CONFIGURED;
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Please enter a question.");
        }

        String context = buildContactContext(user);
        String userPrompt = """
                User question: %s

                Contact data:
                %s
                """.formatted(question.trim(), context);

        return chatModel.call(new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(userPrompt)
        ))).getResult().getOutput().getText();
    }

    public String generateContactNotes(Contact contact) {
        if (chatModel == null) {
            return NOT_CONFIGURED;
        }

        String userPrompt = """
                Write 2-3 short professional notes for this contact.
                Keep it under 80 words. Do not add facts that are not implied by the fields.

                Name: %s
                Nickname: %s
                Email: %s
                Phone: %s
                Work: %s
                Category: %s
                Favorite: %s
                Existing notes: %s
                """.formatted(
                safe(contact.getName()),
                safe(contact.getNickname()),
                safe(contact.getEmail()),
                safe(contact.getPhone()),
                safe(contact.getWork()),
                safe(contact.getCategory()),
                contact.isFavorite(),
                safe(contact.getDescription()));

        return chatModel.call(new Prompt(List.of(
                new SystemMessage("You write concise contact notes for a CRM app."),
                new UserMessage(userPrompt)
        ))).getResult().getOutput().getText();
    }

    public String summarizeContacts(User user) {
        if (chatModel == null) {
            return NOT_CONFIGURED;
        }

        String context = buildContactContext(user);
        String userPrompt = """
                Summarize this user's contact book in 3-5 bullet points.
                Mention totals, categories, favorites, and any patterns you notice.

                Contact data:
                %s
                """.formatted(context);

        return chatModel.call(new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(userPrompt)
        ))).getResult().getOutput().getText();
    }

    private String buildContactContext(User user) {
        List<Contact> contacts = contactRepository.findByUs(user, Pageable.unpaged()).getContent();

        if (contacts.isEmpty()) {
            return "No contacts saved yet.";
        }

        return contacts.stream()
                .map(this::formatContact)
                .collect(Collectors.joining("\n"));
    }

    private String formatContact(Contact contact) {
        return "- ID: SCM%d | Name: %s | Email: %s | Phone: %s | Work: %s | Category: %s | Favorite: %s | Notes: %s"
                .formatted(
                        contact.getCid(),
                        safe(contact.getName()),
                        safe(contact.getEmail()),
                        safe(contact.getPhone()),
                        safe(contact.getWork()),
                        safe(contact.getCategory()),
                        contact.isFavorite() ? "yes" : "no",
                        safe(contact.getDescription()));
    }

    private String safe(String value) {
        return Optional.ofNullable(value).filter(s -> !s.isBlank()).orElse("n/a");
    }
}
