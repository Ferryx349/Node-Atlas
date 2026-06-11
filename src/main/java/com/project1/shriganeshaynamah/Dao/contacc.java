package com.project1.shriganeshaynamah.Dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

@Repository
public interface contacc extends CrudRepository<Contact, Integer> {

    Page<Contact> findByUs(User us, Pageable pageable);

    Page<Contact> findByUsAndNameContainingIgnoreCase(User us, String keyword, Pageable pageable);

    Page<Contact> findByUsAndFavoriteTrue(User us, Pageable pageable);

    Page<Contact> findByUsAndCategory(User us, String category, Pageable pageable);

    Page<Contact> findByUsAndFavoriteTrueAndNameContainingIgnoreCase(User us, String keyword, Pageable pageable);

    Page<Contact> findByUsAndCategoryAndNameContainingIgnoreCase(User us, String category, String keyword, Pageable pageable);

    Page<Contact> findByUsAndCategoryAndFavoriteTrue(User us, String category, Pageable pageable);

    Page<Contact> findByUsAndFavoriteTrueAndCategoryAndNameContainingIgnoreCase(
            User us, String category, String keyword, Pageable pageable);

    long countByUs(User us);

    long countByUsAndFavoriteTrue(User us);

    long countByUsAndCategory(User us, String category);
}
