package com.project1.shriganeshaynamah.Controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project1.shriganeshaynamah.Dao.contacc;
import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.helper.Msg;
import com.project1.shriganeshaynamah.service.ContactService;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class CsvController {

    @Autowired
    private userDao userRepository;

    @Autowired
    private contacc contactRepository;

    @Autowired
    private ContactService contactService;

    @ModelAttribute
    public void addLoggedInUser(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByName(principal.getName()));
        }
    }

    @GetMapping("/user/export/csv")
    public void exportCsv(Principal principal, HttpServletResponse response) throws IOException {
        User user = userRepository.findByName(principal.getName());
        List<Contact> contacts = contactRepository.findByUs(user, Pageable.unpaged()).getContent();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=contacts.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("id,name,email,phone,work,nickname,category,favorite,description");
            for (Contact contact : contacts) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        contact.getCid(),
                        csvEscape(contact.getName()),
                        csvEscape(contact.getEmail()),
                        csvEscape(contact.getPhone()),
                        csvEscape(contact.getWork()),
                        csvEscape(contact.getNickname()),
                        csvEscape(contact.getCategory()),
                        contact.isFavorite(),
                        csvEscape(contact.getDescription()));
            }
        }
    }

    @GetMapping("/user/import")
    public String importPage(Model model, HttpSession session) {
        if (session.getAttribute("message") != null) {
            model.addAttribute("message", session.getAttribute("message"));
            session.removeAttribute("message");
        }
        model.addAttribute("pageTitle", "Import Contacts");
        model.addAttribute("activePage", "import");
        return "user/import";
    }

    @PostMapping("/user/import")
    public String importCsv(
            @RequestParam("file") MultipartFile file,
            Principal principal,
            HttpSession session) throws IOException {

        if (file.isEmpty()) {
            session.setAttribute("message", new Msg("Please select a CSV file.", "alert-danger"));
            return "redirect:/user/import";
        }

        User user = userRepository.findByName(principal.getName());
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null) {
                session.setAttribute("message", new Msg("CSV file is empty.", "alert-danger"));
                return "redirect:/user/import";
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                if (values.size() < 2) {
                    continue;
                }

                Contact contact = new Contact();
                contact.setName(values.get(1));
                if (values.size() > 2) contact.setEmail(values.get(2));
                if (values.size() > 3) contact.setPhone(values.get(3));
                if (values.size() > 4) contact.setWork(values.get(4));
                if (values.size() > 5) contact.setNickname(values.get(5));
                if (values.size() > 6) contact.setCategory(values.get(6));
                if (values.size() > 7) contact.setFavorite(Boolean.parseBoolean(values.get(7)));
                if (values.size() > 8) contact.setDescription(values.get(8));

                if (contact.getName() != null && !contact.getName().isBlank()) {
                    contactService.saveForUser(contact, user);
                    imported++;
                }
            }
        }

        session.setAttribute("message", new Msg(imported + " contact(s) imported successfully.", "alert-success"));
        return "redirect:/user/show/0";
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        return values;
    }
}
