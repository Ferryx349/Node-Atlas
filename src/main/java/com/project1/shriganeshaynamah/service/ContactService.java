package com.project1.shriganeshaynamah.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.project1.shriganeshaynamah.Dao.contacc;
import com.project1.shriganeshaynamah.helper.ContactCategory;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

@Service
public class ContactService {

    @Autowired
    private contacc contactRepository;

    public Page<Contact> findContacts(User user, String keyword, String category, boolean favoritesOnly, Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (favoritesOnly && hasKeyword && hasCategory) {
            return contactRepository.findByUsAndFavoriteTrueAndCategoryAndNameContainingIgnoreCase(
                    user, category.trim(), keyword.trim(), pageable);
        }
        if (favoritesOnly && hasKeyword) {
            return contactRepository.findByUsAndFavoriteTrueAndNameContainingIgnoreCase(user, keyword.trim(), pageable);
        }
        if (favoritesOnly && hasCategory) {
            return contactRepository.findByUsAndCategoryAndFavoriteTrue(user, category.trim(), pageable);
        }
        if (favoritesOnly) {
            return contactRepository.findByUsAndFavoriteTrue(user, pageable);
        }
        if (hasKeyword && hasCategory) {
            return contactRepository.findByUsAndCategoryAndNameContainingIgnoreCase(user, category.trim(), keyword.trim(), pageable);
        }
        if (hasKeyword) {
            return contactRepository.findByUsAndNameContainingIgnoreCase(user, keyword.trim(), pageable);
        }
        if (hasCategory) {
            return contactRepository.findByUsAndCategory(user, category.trim(), pageable);
        }
        return contactRepository.findByUs(user, pageable);
    }

    public Optional<Contact> findOwnedContact(int contactId, User user) {
        return contactRepository.findById(contactId)
                .filter(contact -> contact.getUs().getId() == user.getId());
    }

    public Contact requireOwnedContact(int contactId, User user) {
        return findOwnedContact(contactId, user)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found or access denied"));
    }

    public void applyContactFields(Contact target, Contact source) {
        target.setName(source.getName());
        target.setEmail(source.getEmail());
        target.setPhone(source.getPhone());
        target.setWork(source.getWork());
        target.setDescription(source.getDescription());
        target.setNickname(source.getNickname());
        target.setCategory(normalizeCategory(source.getCategory()));
        target.setFavorite(source.isFavorite());
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }

    public String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return ContactCategory.PERSONAL;
        }
        for (String allowed : ContactCategory.ALL) {
            if (allowed.equalsIgnoreCase(category.trim())) {
                return allowed;
            }
        }
        return ContactCategory.OTHER;
    }

    public Contact saveForUser(Contact contact, User user) {
        contact.setUs(user);
        contact.setCategory(normalizeCategory(contact.getCategory()));
        return contactRepository.save(contact);
    }
}
