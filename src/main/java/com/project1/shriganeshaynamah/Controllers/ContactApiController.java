package com.project1.shriganeshaynamah.Controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project1.shriganeshaynamah.Dao.contacc;
import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.dto.ApiResponse;
import com.project1.shriganeshaynamah.dto.ContactDto;
import com.project1.shriganeshaynamah.helper.ContactCategory;
import com.project1.shriganeshaynamah.service.ContactService;
import com.project1.shriganeshaynamah.service.FileStorageService;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

@RestController
@RequestMapping("/api/contacts")
public class ContactApiController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private userDao userRepository;

    @Autowired
    private contacc contactRepository;

    @Autowired
    private ContactService contactService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listContacts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean favoritesOnly) {

        User user = userRepository.findByName(principal.getName());
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Contact> contacts = contactService.findContacts(user, keyword, category, favoritesOnly, pageable);

        List<ContactDto> items = contacts.getContent().stream()
                .map(ContactDto::from)
                .collect(Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("items", items);
        payload.put("page", contacts.getNumber());
        payload.put("totalPages", contacts.getTotalPages());
        payload.put("totalItems", contacts.getTotalElements());

        return ResponseEntity.ok(ApiResponse.ok(payload));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(Principal principal) {
        User user = userRepository.findByName(principal.getName());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", contactRepository.countByUs(user));
        stats.put("favoriteContacts", contactRepository.countByUsAndFavoriteTrue(user));
        stats.put("workContacts", contactRepository.countByUsAndCategory(user, ContactCategory.WORK));
        stats.put("personalContacts", contactRepository.countByUsAndCategory(user, ContactCategory.PERSONAL));
        stats.put("familyContacts", contactRepository.countByUsAndCategory(user, ContactCategory.FAMILY));

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactDto>> getContact(@PathVariable int id, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        Contact contact = contactService.requireOwnedContact(id, user);
        return ResponseEntity.ok(ApiResponse.ok(ContactDto.from(contact)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactDto>> createContact(
            @RequestBody ContactDto dto, Principal principal) {

        if (dto.getName() == null || dto.getName().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
        }

        User user = userRepository.findByName(principal.getName());
        Contact contact = new Contact();
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setWork(dto.getWork());
        contact.setNickname(dto.getNickname());
        contact.setDescription(dto.getDescription());
        contact.setCategory(dto.getCategory());
        contact.setFavorite(dto.isFavorite());

        Contact saved = contactService.saveForUser(contact, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Contact created", ContactDto.from(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactDto>> updateContact(
            @PathVariable int id,
            @RequestBody ContactDto dto,
            Principal principal) {

        User user = userRepository.findByName(principal.getName());
        Contact existing = contactService.requireOwnedContact(id, user);

        Contact updated = new Contact();
        updated.setName(dto.getName());
        updated.setEmail(dto.getEmail());
        updated.setPhone(dto.getPhone());
        updated.setWork(dto.getWork());
        updated.setNickname(dto.getNickname());
        updated.setDescription(dto.getDescription());
        updated.setCategory(dto.getCategory());
        updated.setFavorite(dto.isFavorite());

        contactService.applyContactFields(existing, updated);
        Contact saved = contactRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Contact updated", ContactDto.from(saved)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable int id, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        Contact contact = contactService.requireOwnedContact(id, user);
        fileStorageService.deleteIfExists(contact.getImage());
        contactService.deleteContact(contact);
        return ResponseEntity.ok(ApiResponse.ok("Contact deleted", null));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ApiResponse<ContactDto>> uploadContactImage(
            @PathVariable int id,
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        User user = userRepository.findByName(principal.getName());
        Contact contact = contactService.requireOwnedContact(id, user);

        try {
            fileStorageService.deleteIfExists(contact.getImage());
            contact.setImage(fileStorageService.storeContactImage(file, user.getId()));
            Contact saved = contactRepository.save(contact);
            return ResponseEntity.ok(ApiResponse.ok("Image uploaded", ContactDto.from(saved)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<ApiResponse<ContactDto>> deleteContactImage(@PathVariable int id, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        Contact contact = contactService.requireOwnedContact(id, user);
        fileStorageService.deleteIfExists(contact.getImage());
        contact.setImage(null);
        Contact saved = contactRepository.save(contact);
        return ResponseEntity.ok(ApiResponse.ok("Image removed", ContactDto.from(saved)));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<ContactDto>> toggleFavorite(@PathVariable int id, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        Contact contact = contactService.requireOwnedContact(id, user);
        contact.setFavorite(!contact.isFavorite());
        Contact saved = contactRepository.save(contact);
        return ResponseEntity.ok(ApiResponse.ok("Favorite updated", ContactDto.from(saved)));
    }

}
