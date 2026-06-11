package com.project1.shriganeshaynamah.Controllers;

import java.security.Principal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project1.shriganeshaynamah.Dao.contacc;
import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.helper.ContactCategory;
import com.project1.shriganeshaynamah.helper.Msg;
import com.project1.shriganeshaynamah.service.ContactService;
import com.project1.shriganeshaynamah.service.FileStorageService;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);
    private static final int PAGE_SIZE = 5;

    @Autowired
    private userDao userRepository;

    @Autowired
    private contacc contactRepository;

    @Autowired
    private ContactService contactService;

    @Autowired
    private FileStorageService fileStorageService;

    @ModelAttribute
    public void addLoggedInUser(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByName(principal.getName()));
        }
    }

    private void setPageMeta(Model model, String title, String activePage) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("activePage", activePage);
    }

    @GetMapping("/user/index")
    public String dashboard(Model model, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        model.addAttribute("contactCount", contactRepository.countByUs(user));
        model.addAttribute("favoriteCount", contactRepository.countByUsAndFavoriteTrue(user));
        model.addAttribute("workCount", contactRepository.countByUsAndCategory(user, ContactCategory.WORK));
        model.addAttribute("personalCount", contactRepository.countByUsAndCategory(user, ContactCategory.PERSONAL));
        setPageMeta(model, "Dashboard", "dashboard");
        return "user/index";
    }

    @GetMapping("/addcontact")
    public String addContactPage(Model model) {
        model.addAttribute("contact", new Contact());
        model.addAttribute("categories", ContactCategory.ALL);
        setPageMeta(model, "Add Contact", "add");
        return "user/add-contact";
    }

    @PostMapping("/user/a-contact")
    public String saveContact(
            @ModelAttribute Contact contact,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Principal principal,
            HttpSession session) {

        User user = userRepository.findByName(principal.getName());

        try {
            if (photo != null && !photo.isEmpty()) {
                contact.setImage(fileStorageService.storeContactImage(photo, user.getId()));
            }
            contactService.saveForUser(contact, user);
            log.info("Contact saved for user {}: {}", user.getName(), contact.getName());
            session.setAttribute("message", new Msg("Contact added successfully.", "alert-success"));
        } catch (IllegalArgumentException ex) {
            session.setAttribute("message", new Msg(ex.getMessage(), "alert-danger"));
            return "redirect:/addcontact";
        }

        return "redirect:/user/show/0";
    }

    @GetMapping("/user/show/{page}")
    public String listContacts(
            @PathVariable("page") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "favorites", defaultValue = "false") boolean favoritesOnly,
            Model model,
            Principal principal,
            HttpSession session) {

        User user = userRepository.findByName(principal.getName());
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Contact> contacts = contactService.findContacts(user, keyword, category, favoritesOnly, pageable);

        model.addAttribute("contacts", contacts);
        model.addAttribute("nopage", contacts.getTotalPages());
        model.addAttribute("currpage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("favoritesOnly", favoritesOnly);
        model.addAttribute("categories", ContactCategory.ALL);

        if (session.getAttribute("message") != null) {
            model.addAttribute("message", session.getAttribute("message"));
            session.removeAttribute("message");
        }

        setPageMeta(model, "My Contacts", "contacts");
        return "user/scontact";
    }

    @GetMapping("/user/{cid}/ind")
    public String viewContact(
            @PathVariable("cid") int contactId,
            Model model,
            Principal principal,
            HttpSession session) {

        User user = userRepository.findByName(principal.getName());
        Optional<Contact> contactOptional = contactService.findOwnedContact(contactId, user);

        if (contactOptional.isEmpty()) {
            return "redirect:/user/show/0";
        }

        if (session.getAttribute("aiGeneratedNotes") != null) {
            model.addAttribute("aiGeneratedNotes", session.getAttribute("aiGeneratedNotes"));
            session.removeAttribute("aiGeneratedNotes");
        }
        if (session.getAttribute("aiError") != null) {
            model.addAttribute("aiError", session.getAttribute("aiError"));
            session.removeAttribute("aiError");
        }

        model.addAttribute("con", contactOptional.get());
        setPageMeta(model, "Contact Details", "contacts");
        return "user/ind";
    }

    @PostMapping("/user/delete/{cid}")
    public String deleteContact(@PathVariable("cid") int contactId, Principal principal, HttpSession session) {
        User user = userRepository.findByName(principal.getName());
        Optional<Contact> contactOptional = contactService.findOwnedContact(contactId, user);

        if (contactOptional.isEmpty()) {
            session.setAttribute("message", new Msg("You are not authorized to delete this contact.", "alert-danger"));
            return "redirect:/user/show/0";
        }

        Contact contact = contactOptional.get();
        fileStorageService.deleteIfExists(contact.getImage());
        contactService.deleteContact(contact);
        session.setAttribute("message", new Msg("Contact deleted successfully.", "alert-success"));
        return "redirect:/user/show/0";
    }

    @PostMapping("/user/favorite/{cid}")
    public String toggleFavorite(@PathVariable("cid") int contactId, Principal principal, HttpSession session) {
        User user = userRepository.findByName(principal.getName());
        Optional<Contact> contactOptional = contactService.findOwnedContact(contactId, user);

        if (contactOptional.isEmpty()) {
            session.setAttribute("message", new Msg("Contact not found.", "alert-danger"));
            return "redirect:/user/show/0";
        }

        Contact contact = contactOptional.get();
        contact.setFavorite(!contact.isFavorite());
        contactRepository.save(contact);

        String status = contact.isFavorite() ? "added to" : "removed from";
        session.setAttribute("message", new Msg("Contact " + status + " favorites.", "alert-success"));
        return "redirect:/user/" + contactId + "/ind";
    }

    @PostMapping("/user/upd/{cid}")
    public String editContactPage(@PathVariable("cid") int contactId, Model model, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        Optional<Contact> contactOptional = contactService.findOwnedContact(contactId, user);

        if (contactOptional.isEmpty()) {
            return "redirect:/user/show/0";
        }

        model.addAttribute("con", contactOptional.get());
        model.addAttribute("cp", contactOptional.get());
        model.addAttribute("categories", ContactCategory.ALL);
        setPageMeta(model, "Update Contact", "contacts");
        return "user/update-page";
    }

    @PostMapping("/user/upt")
    public String updateContact(
            @ModelAttribute("cp") Contact updatedContact,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "removePhoto", defaultValue = "false") boolean removePhoto,
            HttpSession session,
            Principal principal) {

        User user = userRepository.findByName(principal.getName());
        Optional<Contact> contactOptional = contactService.findOwnedContact(updatedContact.getCid(), user);

        if (contactOptional.isEmpty()) {
            session.setAttribute("message", new Msg("You are not authorized to update this contact.", "alert-danger"));
            return "redirect:/user/show/0";
        }

        Contact existing = contactOptional.get();

        try {
            if (removePhoto) {
                fileStorageService.deleteIfExists(existing.getImage());
                existing.setImage(null);
            } else if (photo != null && !photo.isEmpty()) {
                fileStorageService.deleteIfExists(existing.getImage());
                existing.setImage(fileStorageService.storeContactImage(photo, user.getId()));
            }

            contactService.applyContactFields(existing, updatedContact);
            contactRepository.save(existing);
            session.setAttribute("message", new Msg("Contact updated successfully.", "alert-success"));
        } catch (IllegalArgumentException ex) {
            session.setAttribute("message", new Msg(ex.getMessage(), "alert-danger"));
            return "redirect:/user/" + existing.getCid() + "/ind";
        }

        return "redirect:/user/" + existing.getCid() + "/ind";
    }
}
